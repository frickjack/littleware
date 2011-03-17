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
import littleware.bootstrap.client.AbstractClientModule
import littleware.bootstrap.client.AppBootstrap
import littleware.bootstrap.client.AppModule
import littleware.bootstrap.client.AppModuleFactory
import scala.collection.JavaConversions._

object LittleModuleFactory {

  class LittleModule ( profile:AppBootstrap.AppProfile ) extends AbstractClientModule( profile ) {
    override def configure( binder:Binder ):Unit = {
      binder.bind( classOf[OIdUserCreds.Builder]).to( classOf[internal.OIdUserBuilder] )
      binder.bind( classOf[server.controller.OpenIdTool]
                  ).to( classOf[server.controller.internal.SimpleOidTool]
                  ).in( Scopes.SINGLETON )
      binder.bind( classOf[server.controller.AuthVerifyTool]
                  ).to( classOf[server.controller.internal.InMemoryVerifyTool]
                  ).in( Scopes.SINGLETON )
      binder.bind( classOf[server.model.AuthResponse.Builder] ).to( classOf[server.model.internal.AuthResponseBuilder] )
    }
  }

}

/**
 * Littleware module data for littleware.apps.littleId
 */
class LittleModuleFactory extends AppModuleFactory {
  override def build( profile:AppBootstrap.AppProfile ):AppModule = new LittleModuleFactory.LittleModule( profile )
}