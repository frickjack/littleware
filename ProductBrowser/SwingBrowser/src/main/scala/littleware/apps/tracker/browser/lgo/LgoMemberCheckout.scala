/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser.lgo

import com.google.common.collect.ImmutableMap
import com.google.inject.{Inject,Provider}
import java.io.File
import littleware.apps.tracker.Member
import littleware.apps.tracker.browser.controller.Controller
import littleware.apps.tracker.browser.model.MemberCheckoutData
import littleware.asset.{AssetPathFactory, AssetSearchManager}
import littleware.base.feedback.Feedback
import littleware.lgo.{AbstractLgoBuilder,AbstractLgoCommand}

object LgoMemberCheckout {
  object Option {
    val member = "member"
    val destination = "destination"
  }

  class Builder @Inject() ( controller:Controller,
                           dataProvider:Provider[MemberCheckoutData.Builder],
                           search:AssetSearchManager,
                           pathFactory:AssetPathFactory
  ) extends AbstractLgoBuilder[MemberCheckoutData]( classOf[LgoMemberCheckout].getName ) {
    override def buildSafe( input:MemberCheckoutData ):LgoMemberCheckout =
      new LgoMemberCheckout( controller, input )
    
    override def buildFromArgs( args:java.util.List[String] ):LgoMemberCheckout = {
      val argMap = AbstractLgoBuilder.processArgs(args,
                                                  ImmutableMap.of( Option.member, "",
                                                                  Option.destination, ""
        )
      )
      val checkoutData = dataProvider.get.destinationDir( new File( argMap get Option.destination )
      ).member( 
        search.getAssetAtPath( pathFactory.createPath(argMap get Option.member)).get.narrow( classOf[Member])
      ).build
      buildSafe( checkoutData )
    }
  }
}

class LgoMemberCheckout (
  controller:Controller,
  data:MemberCheckoutData
) extends AbstractLgoCommand[MemberCheckoutData,File]( classOf[LgoMemberCheckout].getName, data ) {

  override def runCommand( feedback:Feedback ):File = {
    controller.checkout( data, feedback )
    data.destinationDir
  }
}
