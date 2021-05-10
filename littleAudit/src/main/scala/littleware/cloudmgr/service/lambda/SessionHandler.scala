package littleware.cloudmgr.service.lambda


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

import com.google.gson
import com.google.inject

import java.util.UUID

import littleware.bootstrap.AppBootstrap
import littleware.cloudmgr.service.SessionMgr
import littleware.cloudutil.{ LRN, Session }
import littleware.scala.JsonConfigLoader

import scala.jdk.CollectionConverters._
import scala.util.{ Try, Success, Failure }

/**
 * Adapted from https://github.com/awsdocs/aws-lambda-developer-guide/blob/main/sample-apps/java-events/src/main/java/example/HandlerApiGateway.java
 */
class SessionHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent]{
  import SessionHandler.tools

  override def handleRequest(event:APIGatewayProxyRequestEvent, context:Context):APIGatewayProxyResponseEvent = {
    val logger = context.getLogger()
      
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
        "Content-Type" -> "application/json"
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
        SessionHandler.authHeaderName
      ).map(
        tok => tok.replaceAll(raw"^bearer\s+", "")
      ).orElse(
        { cookies.get(SessionHandler.authCookieName) }
      )

    def sessionFromToken(projectId: UUID):Option[Session] = reqHeads.get(
        SessionHandler.authHeaderName
      ).map(
        tok => tok.replaceAll(raw"^bearer\s+", "")
      ).orElse(
        { cookies.get(SessionHandler.sessionCookieName(projectId.toString())) }
      ).flatMap(
        tok => tools.sessionMgr.jwsToSession(tok) match {
          case Success(session) => Some(session)
          case Failure(ex) => {
            logger.log("Failed to parse token header")
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
        response.withStatusCode(200)
      }
      case method => event.getPath() match {
        case SessionHandler.sessionPattern(projIdStr) => {
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
                  case LRN.zeroId => authToken match {
                    case Some(jwsToken) => {
                      Try(tools.sessionMgr.startSession(jwsToken, projId, "*")) match {
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
                              SessionHandler.sessionCookieName(projId.toString()),
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
                          response.withStatusCode(401
                          ).withBody(s"""{ "message": "invalid creds" }""".trim()
                          )
                        }
                      }
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

object SessionHandler {
    val gs = new gson.GsonBuilder().setPrettyPrinting().create()

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
          val logger = context.getLogger()
          // log execution details
          logger.log("ENVIRONMENT VARIABLES: " + gs.toJson(System.getenv()))
          logger.log("CONTEXT: " + gs.toJson(context))
          // log event details
          logger.log("EVENT: " + gs.toJson(event))
          logger.log("EVENT TYPE: " + event.getClass().toString())
      }
    }

    val tools: Tools = {
      val boot = AppBootstrap.appProvider.get().profile(AppBootstrap.AppProfile.WebApp).build()
      boot.bootstrap(classOf[Tools])
    }

    val authHeaderName = "authorization"
    val authCookieName = "__Secure-Authorization"

    def sessionCookieName(projectId:String) = "__Secure-Session-" + projectId

    // check https://github.com/frickjack/misc-stuff
    // AWS/lib/cloudformation/cloud/api/authclient/sessionMgrOpenApi.json
    //
    val sessionPattern = "session/([^/ ]+)$".r

}
