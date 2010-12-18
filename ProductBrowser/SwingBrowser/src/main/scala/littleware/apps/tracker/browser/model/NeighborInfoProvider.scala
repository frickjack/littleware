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

import com.google.inject.Provider
import littleware.apps.client.AssetModel
import littleware.asset.AssetPath

object NeighborInfoProvider {
  class SimpleNeighbor(
    override val model:AssetModel,
    override val absPath:AssetPath,
    override val relativePath:String
  ) extends NeighborInfo {}

  class SimpleBuilder extends NeighborInfo.Builder {
    override def build = new SimpleNeighbor( model, absPath, relativePath )
  }
}
class NeighborInfoProvider extends Provider[NeighborInfo.Builder] {
  override def get = new NeighborInfoProvider.SimpleBuilder
}
