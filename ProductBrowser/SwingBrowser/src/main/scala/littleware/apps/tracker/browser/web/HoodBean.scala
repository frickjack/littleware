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
import java.util.UUID
import littleware.apps.tracker.browser.controller
import littleware.web.beans.InjectMeBean

/**
 * Backing bean manages data model and user interactions
 * for simple read-only product browser
 */
class HoodBean extends InjectMeBean {

  private var tool:controller.Controller = null
  @Inject()
  def injectMe( tool:controller.Controller ):Unit = {
    this.tool = tool
  }

  def loadHood( assetId:UUID ):Unit = {
    val hood = tool.loadNeighborhood(assetId)
  }
}
