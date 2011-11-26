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

import littleware.apps.client.AssetModel
import littleware.asset.AssetPath

trait NeighborInfo {
  val model:AssetModel
  def getModel = model
  val absPath:AssetPath
  def getAbsPath = absPath
  val relativePath:String
  def getRelativePath = relativePath
}

object NeighborInfo {
  trait Builder {
    var model:AssetModel = null
    def model( value:AssetModel ):this.type = {
      model = value
      this
    }
    var absPath:AssetPath = null
    def absPath( value:AssetPath ):this.type = {
      absPath = value
      this
    }
    var relativePath:String = null
    def relativePath( value:String ):this.type = {
      relativePath =value
      this
    }

    def build:NeighborInfo
  }
}
