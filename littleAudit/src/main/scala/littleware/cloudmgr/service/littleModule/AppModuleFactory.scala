package littleware.cloudmgr.service.littleModule

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

  class AppModule ( profile:bootstrap.AppBootstrap.AppProfile ) extends bootstrap.helper.AbstractAppModule( profile ) {
    
    /**
     * load properties from the LITTLEWARE_CLOUDMGR environment
     * if present, otherwise
     * some other mechanism (ex: littleware.properties) 
     * must bind the following:
     * <ul>
     * <li>little.cloudmgr.domain 
     */
    override def configure( binder:inject.Binder ):Unit = {
      Option(System.getenv("LITTLEWARE_CLOUDMGR")) map {
          lambdaConfigStr =>
          val helper = new cloudutil.ConfigHelper.BindHelper(binder)
          cloudutil.ConfigHelper.loadJsonMap(lambdaConfigStr).foreach(
            kv => helper.bindKeyValue(kv._1, kv._2)
          )
        } getOrElse {
          log.log(Level.WARNING, "LITTLEWARE_CLOUDMGR environment not defined - falls back to littleware.properties")
        }

      // so far able to get by with annotation based bindings
      //binder.bind(classOf[Config]).toProvider(classOf[ConfigLoader]).in(inject.Scopes.SINGLETON)
      //binder.bind( classOf[model.Response.Builder] ).to( classOf[model.internal.ResponseBuilder])
    }
    
  }

}
