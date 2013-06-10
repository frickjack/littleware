/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId

import com.google.inject
import littleware.base.Options
import littleware.bootstrap.{AppBootstrap,AppModule,AppModuleFactory,LittleModule,helper}
import littleware.asset.gson.LittleGsonFactory
import scala.collection.JavaConversions._



object LittleModuleFactory {
  
  /**
   * Registers custom gson adapters at startup
   */
  class ModuleActivator @inject.Inject() (
    gsonFactory:LittleGsonFactory,
    authRequestAdapter:gsonAdapter.AuthRequestAdapter,
    authStateAdapter:gsonAdapter.AuthStateAdapter,
    dataForProviderAdapter:gsonAdapter.DataForProviderAdapter,
    oidCredsAdapter:gsonAdapter.OIdCredsAdapter
  ) extends LittleModule.LifecycleCallback {
    import server.model._
    Seq( classOf[AuthRequest] -> authRequestAdapter,
        classOf[AuthState] -> authStateAdapter,
        classOf[DataForProvider] -> dataForProviderAdapter,
        classOf[common.model.OIdUserCreds] -> oidCredsAdapter
        ).foreach( {
            case (clazz,adapter) => gsonFactory.registerTypeAdapter( clazz, adapter )
          })
    
    def startUp() = {}

    def shutDown() = {}

  }

  class LittleModule ( profile:AppBootstrap.AppProfile ) extends helper.AbstractAppModule( profile ) {
    override def  getCallback():littleware.base.Option[Class[ModuleActivator]] = Options.some( classOf[ModuleActivator] )

    override def configure( binder:inject.Binder ):Unit = {
      binder.bind( classOf[server.controller.OpenIdTool]
                  ).to( classOf[server.controller.internal.SimpleOidTool]
                  ).in( inject.Scopes.SINGLETON )
      binder.bind( classOf[client.controller.VerifyTool]
        ).to( classOf[client.controller.internal.InMemoryVerifyTool]
        ).in( inject.Scopes.SINGLETON )
      binder.bind( classOf[client.controller.internal.InMemoryVerifyTool] 
        ).in( inject.Scopes.SINGLETON )
      littleware.base.PropertiesGuice.build(
        classOf[client.controller.internal.HttpVerifyTool]
      ).configure( binder )
    }
  }

}

/**
 * Littleware module data for littleware.apps.littleId
 */
class LittleModuleFactory extends AppModuleFactory {
  override def build( profile:AppBootstrap.AppProfile ):AppModule = new LittleModuleFactory.LittleModule( profile )
}