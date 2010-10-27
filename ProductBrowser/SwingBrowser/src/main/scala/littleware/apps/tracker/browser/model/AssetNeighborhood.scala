/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser.model

/**
 * Summary object holds assets in the neighborhood
 * of a core asset.
 */
import java.util.UUID
import littleware.apps.client.AssetModel
import littleware.asset.AssetPath


object AssetNeighborhood {
  trait Builder {
    var asset:AssetInfo = null
    def asset( info:AssetInfo ):this.type = {
      asset = info
      this
    }
    
    var children:Seq[AssetInfo] = Nil
    def addChild( info:AssetInfo ):this.type = {
      children = children :+ info
      this
    }
    def children( value:Seq[AssetInfo] ):this.type = {
      children = value
      this
    }
    
    var uncles:Seq[AssetInfo] = Nil
    def addUncle( info:AssetInfo ):this.type = {
      uncles = uncles :+ info
      this
    }
    def uncles( value:Seq[AssetInfo] ):this.type = {
      uncles = value
      this
    }

    var siblings:Seq[AssetInfo] = Nil
    def addSibling( info:AssetInfo ):this.type = {
      siblings = siblings :+ info
      this
    }
    def siblings( value:Seq[AssetInfo] ):this.type = {
      siblings = value
      this
    }
    
    var neighbors:Map[UUID,AssetInfo] = Map.empty
    def neighbors( value:Map[UUID,AssetInfo] ):this.type = {
      this.neighbors = value
      this
    }
    def addNeighbor( info:AssetInfo ):this.type = {
      neighbors = neighbors + (info.model.getAsset.getId -> info)
      this
    }

    def build:AssetNeighborhood
  }

  case class AssetInfo (
    model:AssetModel,
    path:AssetPath
    ) {}
    
}

import AssetNeighborhood._

trait AssetNeighborhood {
  val asset:AssetInfo
  val children:Seq[AssetInfo]
  val uncles:Seq[AssetInfo]
  val siblings:Seq[AssetInfo]
  val neighbors:Map[UUID,AssetInfo]
}
