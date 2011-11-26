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
import littleware.apps.tracker.{Product,Version, Member}
import littleware.apps.lgo.AbstractAssetCommand
import littleware.apps.tracker.browser.controller.Controller
import littleware.apps.tracker.browser.model
import littleware.asset.pickle.HumanPicklerProvider
import littleware.asset.{AssetSearchManager,AssetPathFactory}
import littleware.base.feedback.Feedback
import littleware.lgo.AbstractLgoBuilder
import littleware.lgo.AbstractLgoCommand
import scala.collection.JavaConversions._

object LgoCreateMember {
  object Option {
    val version = "version"
    val name = "name"
    val comment = "comment"
    val data = "data"
  }

  class Builder @Inject() ( controller:Controller,
                           search:AssetSearchManager,
                           pathFactory:AssetPathFactory,
                           dataProvider:Provider[model.MemberData.Builder],
                           pickleProvider:HumanPicklerProvider
  ) extends AbstractLgoBuilder[model.MemberData]( classOf[LgoCreateMember].getName ) {
    override def buildSafe( input:model.MemberData ):LgoCreateMember = new LgoCreateMember( controller, pickleProvider, input )
    override def buildFromArgs( args:java.util.List[String] ):LgoCreateMember = {
      val argMap = AbstractLgoBuilder.processArgs( args,
                                                  ImmutableMap.of( Option.version, "",
                                                      Option.name, "",
                                                      Option.comment, "",
                                                      Option.data, ""
                     )
      )
      buildSafe( dataProvider.get.
                version( search.getAssetAtPath( pathFactory.createPath( argMap.get( Option.version )) ).get.narrow( classOf[Version] )
        ).name( argMap get Option.name
        ).comment( argMap get Option.comment
        ).data( argMap get Option.data
        ).build
      )
    }
  }
}

class LgoCreateMember( controller:Controller,
                       pickleProvider:HumanPicklerProvider,
                       input:model.MemberData
) extends AbstractAssetCommand[model.MemberData,Member](
  classOf[LgoCreateMember].getName, pickleProvider, input
) {

  override def runCommand( feedback:Feedback ):Member = controller.createMember(input)
}
