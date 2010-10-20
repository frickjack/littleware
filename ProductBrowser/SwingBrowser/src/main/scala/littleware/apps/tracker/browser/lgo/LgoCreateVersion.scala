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
import littleware.apps.tracker.{Product,Version}
import littleware.apps.lgo.AbstractAssetCommand
import littleware.apps.tracker.browser.controller.Controller
import littleware.apps.tracker.browser.model
import littleware.asset.pickle.HumanPicklerProvider
import littleware.asset.{AssetSearchManager,AssetPathFactory}
import littleware.base.feedback.Feedback
import littleware.lgo.AbstractLgoBuilder
import littleware.lgo.AbstractLgoCommand
import scala.collection.JavaConversions._

object LgoCreateVersion {
  object Option {
    val product = "product"
    val name = "name"
    val comment = "comment"
  }

  class Builder @Inject() ( controller:Controller,
                           search:AssetSearchManager,
                           pathFactory:AssetPathFactory,
                           dataProvider:Provider[model.VersionData.Builder],
                           pickleProvider:HumanPicklerProvider
  ) extends AbstractLgoBuilder[model.VersionData]( classOf[LgoCreateVersion].getName ) {
    override def buildSafe( input:model.VersionData ):LgoCreateVersion = new LgoCreateVersion( controller, pickleProvider, input )
    override def buildFromArgs( args:java.util.List[String] ):LgoCreateVersion = {
      val argMap = AbstractLgoBuilder.processArgs( args,
                                                  ImmutableMap.of( Option.product, "",
                                                      Option.name, "",
                                                      Option.comment, "" )
      )
      buildSafe( dataProvider.get.
                product( search.getAssetAtPath( pathFactory.createPath( argMap.get( Option.product )) ).get.narrow( classOf[Product] )
        ).name( argMap get Option.name
        ).comment( argMap get Option.comment
        ).build
      )
    }
  }
}

class LgoCreateVersion( controller:Controller,
                       pickleProvider:HumanPicklerProvider,
                       input:model.VersionData
) extends AbstractAssetCommand[model.VersionData,Version](
  classOf[LgoCreateVersion].getName, pickleProvider, input
) {
  
  override def runCommand( feedback:Feedback ):Version = controller.createVersion(input)
}
