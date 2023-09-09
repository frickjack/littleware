package littleware.cabinet.littleModule

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
  val CONFIG_KEY = "littleware/cabinet/CABINET"

  class AppModule (profile:bootstrap.AppBootstrap.AppProfile) extends bootstrap.helper.AbstractAppModule( profile ) {    
    /**
     * load properties via littleware.scala.JsonConfigLoader
     */
    override def configure(binder: inject.Binder):Unit = {
      littleware.scala.JsonConfigLoader.loadConfig(CONFIG_KEY, getClass().getClassLoader()).map(
        {
          jsConfig =>
          littleware.scala.JsonConfigLoader.bindKeys(binder, jsConfig)
        }
      )
    }
    
  }

}
