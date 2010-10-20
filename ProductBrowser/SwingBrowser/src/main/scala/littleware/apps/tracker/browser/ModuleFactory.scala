/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser

import com.google.inject.Binder
import com.google.inject.Scopes
import littleware.bootstrap.client.AbstractClientModule
import littleware.bootstrap.client.AppBootstrap
import littleware.bootstrap.client.AppModule
import littleware.bootstrap.client.ClientModule
import littleware.bootstrap.client.ClientModuleFactory
import littleware.lgo.LgoCommand
import littleware.lgo.LgoServiceModule
import scala.collection.JavaConversions._

class ModuleFactory extends ClientModuleFactory {
  class LgoModule( profile:AppBootstrap.AppProfile
  ) extends AbstractClientModule( profile ) with LgoServiceModule {
    val lgoCommand = List( classOf[lgo.LgoCreateProduct.Builder],
                          classOf[lgo.LgoCreateVersion.Builder],
                          classOf[lgo.LgoCreateMember.Builder],
                          classOf[lgo.LgoMemberCheckin.Builder],
                          classOf[lgo.LgoMemberCheckout.Builder]
          )

    override def getLgoCommands:java.util.Collection[Class[_ <: LgoCommand.LgoBuilder] ] = lgoCommand

    override def configure( binder:Binder ):Unit = {
      binder.bind( classOf[controller.Controller]).to( classOf[controller.SimpleController]
                                                      ).in( Scopes.SINGLETON )
      binder.bind( classOf[model.ProductData.Builder] ).toProvider( model.ProductDataProvider )
      binder.bind( classOf[model.VersionData.Builder] ).toProvider( model.VersionDataProvider )
      binder.bind( classOf[model.MemberData.Builder] ).toProvider( model.MemberDataProvider )
      binder.bind( classOf[model.MemberCheckinData.Builder] ).toProvider( model.MemberCheckinProvider )
      binder.bind( classOf[model.MemberCheckoutData.Builder] ).toProvider( model.MemberCheckoutProvider )
    }
  }

  override def build( profile:AppBootstrap.AppProfile ):ClientModule = new LgoModule( profile )
}
