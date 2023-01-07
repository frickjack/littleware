package littleware.scala.littleModule

import com.google.gson
import com.google.inject
import java.util.logging.{Level,Logger}

import littleware.bootstrap
import littleware.{ scala => littleScala }

  
class AppModuleFactory extends bootstrap.AppModuleFactory {
  override def build(profile:bootstrap.AppBootstrap.AppProfile):bootstrap.AppModule = new AppModuleFactory.AppModule(profile)
}

  
object AppModuleFactory {
  val log = Logger.getLogger(classOf[AppModuleFactory].getName())
  
  class AppModule ( profile:bootstrap.AppBootstrap.AppProfile ) extends bootstrap.helper.AbstractAppModule( profile ) {    
    override def configure(binder: inject.Binder):Unit = {
      binder.bind(classOf[gson.Gson]).toProvider(littleScala.GsonProvider)
      binder.bind(classOf[littleScala.GsonProvider]).toInstance(littleScala.GsonProvider)
      //binder.bind(classOf[Config]).toProvider(classOf[ConfigLoader]).in(inject.Scopes.SINGLETON)
      //binder.bind( classOf[model.Response.Builder] ).to( classOf[model.internal.ResponseBuilder])
    }
    
  }

}
