package littleware.cloudmgr.service.lambda


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

import com.google.gson
import com.google.inject

import java.util.UUID
import java.util.logging.{ Logger, Level }
import littleware.bootstrap.AppBootstrap
import littleware.cloudmgr.service.SessionMgr
import littleware.cloudutil.{ LRN, Session }
import littleware.scala.JsonConfigLoader

import scala.jdk.CollectionConverters._
import scala.util.{ Try, Success, Failure }

/**
 * Adapted from https://github.com/awsdocs/aws-lambda-developer-guide/blob/master/sample-apps/java-events/src/main/java/example/HandlerApiGateway.java
 */
class SessionLambda @inject.Inject()(tools:SessionLambda.Tools) extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent]{
  import SessionLambda.log

  def this() = this(SessionLambda.tools)

  override def handleRequest(event:APIGatewayProxyRequestEvent, cx:Context):APIGatewayProxyResponseEvent = {      
    // headers normalized with lowercase keys
    val reqHeads:Map[String, String] = Option(event.getHeaders()).toSeq.flatMap(
      { 
        jmap =>
        jmap.asScala.map(
          kv => kv._1.toLowerCase() -> kv._2
        )
      }
    ).toMap

    val origin:Option[String] = reqHeads.get("origin")
    val cookies:Map[String, String] = reqHeads.get("cookie"
      ).map(cookieStr => LambdaHelper.parseCookies(cookieStr)
      ).getOrElse(Map.empty)

    val commonHeaders = Map(
        "Content-Type" -> "application/json",
        "Strict-Transport-Security" -> "max-age=31536000",
        "Cache-Control" -> "no-store, max-age=0"
        )

    val corsHeaders:Map[String, String] =
        origin.toSeq.flatMap(
          {
            urlStr => 
            val hostname = new java.net.URL(urlStr).getHost()
            tools.config.corsDomainWhiteList.find(
              rule => hostname.endsWith(rule)
            ).toSeq.flatMap(
              {
                _ => Seq(
                    "Access-Control-Allow-Credentials" -> "true",
                    "Access-Control-Allow-Headers" -> "Accept,Authorization",
                    "Access-Control-Allow-Methods" -> "GET,POST,PUT,DELETE",
                    "Access-Control-Allow-Origin" -> urlStr,
                    "Access-Control-Max-Age" -> "86400"
                )
              }
            )
          }
        ).toMap

    val authToken:Option[String] = reqHeads.get(
        SessionLambda.authHeaderName
      ).map(
        tok => tok.replaceAll(raw"^bearer\s+", "")
      ).orElse(
        { cookies.get(SessionLambda.authCookieName) }
      )

    def sessionFromToken(projectId: UUID):Option[Session] = reqHeads.get(
        SessionLambda.authHeaderName
      ).map(
        tok => tok.replaceAll(raw"^bearer\s+", "")
      ).orElse(
        { cookies.get(SessionLambda.sessionCookieName(projectId.toString())) }
      ).flatMap(
        tok => tools.sessionMgr.jwsToSession(tok) match {
          case Success(session) => Some(session)
          case Failure(ex) => {
            log.log(Level.INFO, "Failed to parse token header")
            None
          }
        }
      ).filter(
        session => {
          session.projectId.equals(projectId)
        }
      )

    // default response - properties updated below
    val response = new APIGatewayProxyResponseEvent().withStatusCode(500
          ).withHeaders((commonHeaders ++ corsHeaders).asJava
          ).withBody(s"""{ "message": "something went wrong" }"""
          ).withIsBase64Encoded(false)
    
    event.getHttpMethod() match {
      case "OPTIONS" => {
        response.withStatusCode(200
        ).withBody(s"""{ "message": "options ok" }"""
        )
      }
      case method => event.getPath() match {
        case SessionLambda.sessionPattern(projIdStr) => {
          Try({ UUID.fromString(projIdStr) }) match {
            case Success(projId) => {
              method match {
                case "GET" => sessionFromToken(projId) match {
                  case Some(session) => {
                    response.withStatusCode(200
                    ).withBody(tools.gs.toJson(session)
                    )
                  }
                  case None => {
                    response.withStatusCode(401
                    ).withBody(s"""{ "message": "invalid creds" }""".trim()
                    )
                  }
                }
                case "POST" => projId match {
                  // we currently just have one project ...
                  case LRN.zeroId => authToken match {
                    case Some(jwsToken) => {
                      Try(tools.sessionMgr.startSession(jwsToken, projId, "little-wildcard")) match {
                        case Success(session) => {
                          val sessionToken = tools.sessionMgr.sessionToJws(session)
                          // either put the session in a cookie, or in the response
                          // depending on how the client authenticated
                          if (reqHeads.contains("authorization")) {
                            response.withStatusCode(200
                            ).withBody(
                              s"""{
                                "token": "${ sessionToken }",
                                "session": ${ tools.gs.toJson(session) }
                              }""".replaceAll(raw"\n\s+", "\n  ")
                            )
                          } else {
                            val cookie = LambdaHelper.CookieInfo(
                              SessionLambda.sessionCookieName(projId.toString()),
                              sessionToken,
                              Some(tools.config.cookieDomain),
                              3600
                            )
                            response.withStatusCode(200
                            ).withBody(
                              s"""{
                                "token": null,
                                "session": ${ tools.gs.toJson(session) }
                              }""".replaceAll(raw"\n\s+", "\n  ")
                            ).withHeaders(
                              (
                                commonHeaders ++
                                corsHeaders ++
                                Map("Set-Cookie" -> cookie.toString())
                              ).asJava
                            )
                          }
                        }
                        case Failure(ex) => {
                          log.log(Level.WARNING, "failed to setup session", ex)
                          response.withStatusCode(401
                          ).withBody(s"""{ "message": "invalid creds" }""".trim()
                          )
                        }
                      }
                    }
                    case None => {
                      response.withStatusCode(401
                      ).withBody(s"""{ "message": "no creds" }""".trim()
                      )
                    }
                  }
                  case otherId => {
                    response.withStatusCode(400
                    ).withBody(s"""
                      { "message": "currently only support the zero project" }
                      """.trim()
                    )
                  }
                }
              }
            }
            case Failure(ex) => {
              response.withStatusCode(404
                ).withBody(s"""{ "message": "invalid project id" }""".trim()
                )
            }
          }
        }
        case SessionLambda.versionPattern() => {
          response.withStatusCode(200
          ).withBody(s"""{ "message": "version info", "semver": "3.0.1" }""".trim()
          )
        }
        case other => {
          response.withStatusCode(400
          ).withBody(s"""{ "message": "unknown path" }""".trim()
          )
        }
      }
    }

    // log execution details
    //tools.logEnvironment(event, context)
    response
  }
}

//----------------------

object SessionLambda {
    val gs = new gson.GsonBuilder().setPrettyPrinting().create()
    val log = Logger.getLogger(classOf[SessionLambda].getName())

    /**
     * Additional configuration for the lambda
     *
     * @param clientWhitelist parents of domains trusted for CORS access
     */
     @inject.Singleton()
     @inject.ProvidedBy(classOf[ConfigProvider])
    case class Config (
      corsDomainWhiteList: Seq[String],
      cookieDomain: String
    ) {}

    @inject.Singleton()
    class ConfigProvider @inject.Inject() (
      @inject.name.Named("little.cloudmgr.sessionmgr.lambdaconfig") configStr: String,
      gs: gson.Gson
    ) extends inject.Provider[Config] {
      lazy val singleton = {
        val js = gs.fromJson(configStr, classOf[gson.JsonObject])
        Config(
          js.getAsJsonArray("corsDomainWhiteList"
            ).asScala.toSeq.map({ _.getAsString() }),
          js.getAsJsonPrimitive("cookieDomain").getAsString()
        )
      }

      override def get():Config = singleton
    }


    @inject.Singleton()
    class Tools @inject.Inject()(
      val gs: gson.Gson,
      val config: Config,
      val sessionMgr: SessionMgr
    ) {
      def logEnvironment(event:AnyRef, context:Context):Unit = {
          // log execution details
          log.log(Level.INFO, "ENVIRONMENT VARIABLES: " + gs.toJson(System.getenv()))
          log.log(Level.INFO, "CONTEXT: " + gs.toJson(context))
          // log event details
          log.log(Level.INFO, "EVENT: " + gs.toJson(event))
          log.log(Level.INFO, "EVENT TYPE: " + event.getClass().toString())
      }
    }

    /**
     * Self-bootstrap in lambda environment
     */
    private lazy val tools: Tools = {
      val boot = AppBootstrap.appProvider.get().profile(AppBootstrap.AppProfile.WebApp).build()
      boot.bootstrap(classOf[Tools])
    }

    val authHeaderName = "authorization"
    val authCookieName = "__Secure-Authorization"

    def sessionCookieName(projectId:String) = "__Secure-Session-" + projectId

    // check https://github.com/frickjack/little-automation
    // AWS/lib/cloudformation/cloud/api/authclient/sessionMgrOpenApi.json
    //
    val sessionPattern = ".*/session/([^/ ]+)$".r
    val versionPattern = ".*/version$".r

}
