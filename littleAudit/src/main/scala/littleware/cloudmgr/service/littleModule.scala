package littleware.cloudmgr.service

import com.google.gson
import com.google.inject
import java.util.logging.{Level,Logger}


import littleware.bootstrap.{AppBootstrap,AppModule,AppModuleFactory,helper}


package littleModule {
  
  
  class Factory extends AppModuleFactory {
    override def build(profile:AppBootstrap.AppProfile):AppModule = new LittleAppModule(profile)
  }

  
   
  class LittleAppModule ( profile:AppBootstrap.AppProfile ) extends helper.AbstractAppModule( profile ) {
    
    override def configure( binder:inject.Binder ):Unit = {
      //binder.bind(classOf[SessionMgr]).to(classOf[internal.LocalKeySessionMgr]).in(inject.Scopes.SINGLETON)
      //binder.bind( classOf[model.Response.Builder] ).to( classOf[model.internal.ResponseBuilder])
    }
    
  }  

}
