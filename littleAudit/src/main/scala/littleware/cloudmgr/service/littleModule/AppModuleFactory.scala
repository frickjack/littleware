package littleware.cloudmgr.service.littleModule

import com.google.gson
import com.google.inject
import java.util.logging.{Level,Logger}

import littleware.bootstrap
import scala.jdk.CollectionConverters._

  
class AppModuleFactory extends bootstrap.AppModuleFactory {
  override def build(profile:bootstrap.AppBootstrap.AppProfile):bootstrap.AppModule = new AppModuleFactory.AppModule(profile)
}

  
object AppModuleFactory {
  class AppModule ( profile:bootstrap.AppBootstrap.AppProfile ) extends bootstrap.helper.AbstractAppModule( profile ) {
    
    override def configure( binder:inject.Binder ):Unit = {
      // so far able to get by with annotation based bindings
      //binder.bind(classOf[Config]).toProvider(classOf[ConfigLoader]).in(inject.Scopes.SINGLETON)
      //binder.bind( classOf[model.Response.Builder] ).to( classOf[model.internal.ResponseBuilder])
    }
    
  }
}
