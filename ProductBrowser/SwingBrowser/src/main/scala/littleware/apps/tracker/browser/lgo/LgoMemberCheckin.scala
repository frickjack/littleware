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
import littleware.apps.lgo.AbstractAssetCommand
import littleware.apps.tracker.{Member,Version}
import littleware.apps.tracker.browser.controller.Controller
import littleware.apps.tracker.browser.model.MemberCheckinData
import littleware.asset.{AssetPathFactory,AssetSearchManager}
import littleware.asset.pickle.HumanPicklerProvider
import littleware.base.feedback.Feedback
import littleware.lgo.{AbstractLgoBuilder,AbstractLgoCommand}

object LgoMemberCheckin {
  object Option {
    val version = "version"
    val name    = "name"
    val comment = "comment"
    val datadir = "datadir"
  }

  class Builder @Inject() ( controller:Controller,
                           search:AssetSearchManager,
                           pathFactory:AssetPathFactory,
                           pickleProvider:HumanPicklerProvider,
                           dataProvider:Provider[MemberCheckinData.Builder]
  ) extends AbstractLgoBuilder[MemberCheckinData]( classOf[LgoMemberCheckin].getName )
  {
    override def buildFromArgs( args:java.util.List[String] ):LgoMemberCheckin = {
      val argMap = AbstractLgoBuilder.processArgs( args,
                                                  ImmutableMap.of( Option.version, "",
                                                                  Option.name, "",
                                                                  Option.comment, "",
                                                                  Option.datadir, "" )
      )
      buildSafe(
        dataProvider.get.name( argMap.get( Option.name )
        ).version( search.getAssetAtPath( pathFactory.createPath( argMap get Option.version )).get.narrow( classOf[Version])
        ).comment( argMap get Option.comment
        ).dataDir( new File( argMap get Option.datadir )
        ).build
      )
    }
    override def buildSafe( data:MemberCheckinData ):LgoMemberCheckin =
      new LgoMemberCheckin( controller, pickleProvider, data )
  }
}

class LgoMemberCheckin ( controller:Controller,
                        pickleProvider:HumanPicklerProvider,
                        data:MemberCheckinData
) extends AbstractAssetCommand[MemberCheckinData,Member]( classOf[LgoMemberCheckin].getName, pickleProvider, data) {
  override def runCommand( feedback:Feedback ):Member = controller.checkin(data, feedback)
}
