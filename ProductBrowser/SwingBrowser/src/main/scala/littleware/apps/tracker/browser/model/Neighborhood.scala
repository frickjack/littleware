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


object Neighborhood {
  trait Builder {
    var asset:NeighborInfo = null
    def asset( info:NeighborInfo ):this.type = {
      asset = info
      this
    }
    
    var children:Seq[NeighborInfo] = Nil
    def addChild( info:NeighborInfo ):this.type = {
      children = children :+ info
      this
    }
    def children( value:Seq[NeighborInfo] ):this.type = {
      children = value
      this
    }
    
    var uncles:Seq[NeighborInfo] = Nil
    def addUncle( info:NeighborInfo ):this.type = {
      uncles = uncles :+ info
      this
    }
    def uncles( value:Seq[NeighborInfo] ):this.type = {
      uncles = value
      this
    }

    var siblings:Seq[NeighborInfo] = Nil
    def addSibling( info:NeighborInfo ):this.type = {
      siblings = siblings :+ info
      this
    }
    def siblings( value:Seq[NeighborInfo] ):this.type = {
      siblings = value
      this
    }
    
    var neighbors:Seq[NeighborInfo] = Nil
    def neighbors( value:Seq[NeighborInfo] ):this.type = {
      this.neighbors = value
      this
    }
    def addNeighbor( info:NeighborInfo ):this.type = {
      neighbors = neighbors :+ info
      this
    }

    def build:Neighborhood
  }
    
}


trait Neighborhood {
  val asset:NeighborInfo
  val children:Seq[NeighborInfo]
  val uncles:Seq[NeighborInfo]
  val siblings:Seq[NeighborInfo]
  val neighbors:Seq[NeighborInfo]
}
