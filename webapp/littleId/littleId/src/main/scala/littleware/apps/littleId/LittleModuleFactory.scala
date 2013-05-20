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

import com.google.inject.Binder
import com.google.inject.Scopes
import littleware.bootstrap.{AppBootstrap,AppModule,AppModuleFactory,helper}
import littleware.asset.gson.LittleGsonFactory
import scala.collection.JavaConversions._
import org.osgi

object LittleModuleFactory {
  
  /**
   * Registers custom gson adapters at startup
   */
  class ModuleActivator (
    gsonFactory:LittleGsonFactory,
    authRequestAdapter:gsonAdapter.AuthRequestAdapter,
    authStateAdapter:gsonAdapter.AuthStateAdapter,
    dataForProviderAdapter:gsonAdapter.DataForProviderAdapter
  ) extends osgi.framework.BundleActivator {
    import server.model._
    Seq( classOf[AuthRequest] -> authRequestAdapter,
        classOf[AuthState] -> authStateAdapter,
        classOf[DataForProvider] -> dataForProviderAdapter
        ).foreach( {
            case (clazz,adapter) => gsonFactory.registerTypeAdapter( clazz, adapter )
          })
    
    def start( bc:osgi.framework.BundleContext ):Unit = {}

    def stop( bc:osgi.framework.BundleContext  ):Unit = {}

  }

  class LittleModule ( profile:AppBootstrap.AppProfile ) extends helper.AbstractAppModule( profile ) {
    override def  getActivator():Class[_ <: osgi.framework.BundleActivator] = classOf[ModuleActivator]

    override def configure( binder:Binder ):Unit = {
      binder.bind( classOf[server.controller.OpenIdTool]
                  ).to( classOf[server.controller.internal.SimpleOidTool]
                  ).in( Scopes.SINGLETON )
      binder.bind( classOf[server.controller.AuthVerifyTool]
                  ).to( classOf[server.controller.internal.InMemoryVerifyTool]
                  ).in( Scopes.SINGLETON )
      binder.bind( classOf[client.controller.VerifyTool]
        ).to( classOf[client.controller.internal.HttpVerifyTool]
        ).in( Scopes.SINGLETON )
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