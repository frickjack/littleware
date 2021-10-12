package littleware.cell.pubsub.littleModule

import com.google.gson
import com.google.inject
import java.util.logging.{Level,Logger}

import littleware.bootstrap
import littleware.cloudutil
import scala.jdk.CollectionConverters._

  
class AppModuleFactory extends bootstrap.AppModuleFactory {
  override def build(profile:bootstrap.AppBootstrap.AppProfile):bootstrap.AppModule = new AppModuleFactory.AppModule(profile)
}

  
object AppModuleFactory {
  val log = Logger.getLogger(classOf[AppModuleFactory].getName())
  val CONFIG_KEY = "littleware/cloudmgr/LITTLE_PUBSUB"

  class AppModule (profile:bootstrap.AppBootstrap.AppProfile) extends bootstrap.helper.AbstractAppModule( profile ) {    
    /**
     * load properties from the LITTLE_CELL_PUBSUB key
     * via littleware.scala.JsonConfigLoader
     */
    override def configure(binder: inject.Binder):Unit = {
      littleware.scala.JsonConfigLoader.loadConfig(CONFIG_KEY).map(
        {
          jsConfig =>
          littleware.scala.JsonConfigLoader.bindKeys(binder, jsConfig)
        }
      )
    }
    
  }

}
