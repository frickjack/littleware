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


object NeighborhoodProvider extends Provider[Neighborhood.Builder]{
  override def get:Neighborhood.Builder = new SimpleBuilder
  
  
  class SimpleNeighborhood(
    override val asset:NeighborInfo,
    override val children:Seq[NeighborInfo],
    override val uncles:Seq[NeighborInfo],
    override val siblings:Seq[NeighborInfo],
    override val neighbors:Seq[NeighborInfo]
  ) extends Neighborhood {
  }

  class SimpleBuilder extends Neighborhood.Builder {
    override def build:Neighborhood = new SimpleNeighborhood( asset, children, uncles, siblings, neighbors )
  }
}
