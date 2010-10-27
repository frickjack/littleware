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
import java.util.UUID


object NeighborhoodProvider extends Provider[AssetNeighborhood.Builder]{
  override def get = new Builder
  
  class Builder extends AssetNeighborhood.Builder {
    override def build:AssetNeighborhood = new SimpleNeighborhood( asset, children, uncles,
                                                                  siblings, neighbors
    )
  }

  import AssetNeighborhood.AssetInfo
  
  class SimpleNeighborhood(
    override val asset:AssetInfo,
    override val children:Seq[AssetInfo],
    override val uncles:Seq[AssetInfo],
    override val siblings:Seq[AssetInfo],
    override val neighbors:Map[UUID,AssetInfo]
  ) extends AssetNeighborhood {
  }
}
