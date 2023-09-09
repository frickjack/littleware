package littleware.cell.pubsub.littleModule

import com.google.gson
import com.google.inject
import java.util.logging.{Level,Logger}

import littleware.bootstrap
import littleware.cell.pubsub
import littleware.cloudutil
import scala.jdk.CollectionConverters._

import software.amazon.awssdk.services.dynamodb

  
class AppModuleFactory extends bootstrap.AppModuleFactory {
  override def build(profile:bootstrap.AppBootstrap.AppProfile):bootstrap.AppModule = new AppModuleFactory.AppModule(profile)
}

  
object AppModuleFactory {
  val log = Logger.getLogger(classOf[AppModuleFactory].getName())
  val CONFIG_KEY = "littleware/cell/pubsub/PUBSUB"

  class AppModule (profile:bootstrap.AppBootstrap.AppProfile) extends bootstrap.helper.AbstractAppModule( profile ) {    
    /**
     * load properties from the LITTLE_CELL_PUBSUB key
     * via littleware.scala.JsonConfigLoader
     */
    override def configure(binder: inject.Binder):Unit = {
      val configKeys = littleware.scala.JsonConfigLoader.loadConfig(CONFIG_KEY, getClass().getClassLoader()).map(
          {
            jsConfig =>
            littleware.scala.JsonConfigLoader.bindKeys(binder, jsConfig)
          }
        ).getOrElse(
          Set.empty[String]
        )

      if (
        !configKeys.contains("little.cell.pubsub.awsconfig")
      ) throw new IllegalStateException("pubsub config not bound: " + CONFIG_KEY + ", " + configKeys)

      binder.bind(classOf[dynamodb.DynamoDbClient]).toInstance(
        dynamodb.DynamoDbClient.create()
      )
      binder.bind(classOf[pubsub.service.PubSub]
        ).to(classOf[pubsub.service.internal.DynamoPubSub])
    }
    
  }

}
