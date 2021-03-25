package littleware.cloudmgr.service.littleModule

import com.google.gson
import com.google.inject
import java.util.logging.{Level,Logger}

import littleware.bootstrap.{AppBootstrap,AppModule,AppModuleFactory,helper}
import scala.jdk.CollectionConverters._

@inject.ProvidedBy(classOf[Config.ConfigLoader])
@inject.Singleton()
case class Config (
  // "local" or "aws"
  sessionMgr: String,
  localSessionMgrConfig: Config.LocalSessionConfig
) {}


object Config {
  case class Kid2Pem (kid:String, pem:String) {
    def this(json:gson.JsonObject) = this(
      json.getAsJsonPrimitive("kid").getAsString(),
      json.getAsJsonPrimitive("pem").getAsString()
    )

    override def hashCode():Int = kid.hashCode()
  }

  case class LocalSessionConfig (
    oidcJwksUrl: String,
    signingKey: Option[Kid2Pem],
    verifyKeys: Set[Kid2Pem]
  ) {}


  /**
   * Look for module config from environment.
   */
  class ConfigLoader @inject.Inject() (
    gs: gson.Gson, 
    @inject.name.Named("littleware.cloudmgr.service.littleModule.Config") configStr:String
    ) extends inject.Provider[Config] {

    def fromJson(json:gson.JsonObject): Config = {
      json.getAsJsonPrimitive("sessionMgr").getAsString() match {
        case "local" => {
          val jsLocal = json.getAsJsonObject("localSessionConfig")
          Config(
            "local",
            LocalSessionConfig(
              jsLocal.getAsJsonPrimitive("oidcJwksUrl").getAsString(),
              Option(jsLocal.getAsJsonObject("signingKey")).map({ new Kid2Pem(_) }),
              jsLocal.getAsJsonArray("verifyKeys").asScala.map({ js => new Kid2Pem(js.getAsJsonObject()) }).toSet
            )
          )
        }
      }
    }

    def get():Config = {
      val json = gs.fromJson(configStr, classOf[gson.JsonObject])
      json.getAsJsonPrimitive("configSource").getAsString() match {
        case "this" => {
          fromJson(json)
        }
      }
    }
  }
}
