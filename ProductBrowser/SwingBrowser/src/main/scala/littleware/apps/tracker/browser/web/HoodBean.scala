/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser.web

import com.google.inject.Inject
import edu.auburn.library.util.AuburnHelper
import java.util.UUID
import javax.faces.bean.ManagedBean
import javax.faces.bean.RequestScoped
import littleware.apps.tracker.browser.controller
import littleware.apps.tracker.browser.model.NeighborInfo
import littleware.security.LittleUser
import littleware.web.beans.InjectMeBean
import scala.collection.JavaConversions._

/**
 * Backing bean manages data model and user interactions
 * for simple read-only product browser
 */
@ManagedBean
@RequestScoped
class HoodBean extends InjectMeBean {

  private var tool:controller.Controller = null
  @Inject()
  def injectMe( tool:controller.Controller, user:LittleUser ):Unit = {
    this.tool = tool
    loadHood( user.getHomeId )
  }
  
  
  private var asset:NeighborInfo = null;
  def getAsset = asset

  private var children:java.util.List[NeighborInfo] = java.util.Collections.emptyList[NeighborInfo]
  def getChildren = children

  private var uncles:java.util.List[NeighborInfo] = java.util.Collections.emptyList[NeighborInfo]
  def getUncles = uncles

  private var siblings:java.util.List[NeighborInfo] = java.util.Collections.emptyList[NeighborInfo]
  def getSiblings = siblings

  private var neighbors:java.util.List[NeighborInfo] = java.util.Collections.emptyList[NeighborInfo]
  def getNeighbors = neighbors
  
  def loadHood( assetId:UUID ):Unit = {
    val hood = tool.loadNeighborhood(assetId)
    this.asset = hood.asset
    this.children = hood.children  // implicit conversion thanks to JavaConversions ...
    this.uncles = hood.uncles
    this.siblings = hood.siblings
    this.neighbors = AuburnHelper.toJavaList( hood.neighbors.values )
  }
}
