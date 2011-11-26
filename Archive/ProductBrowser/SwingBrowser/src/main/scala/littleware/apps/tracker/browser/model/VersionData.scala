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

import littleware.apps.tracker.Product

trait VersionData {
  val product:Product
  val name:String
  val comment:String
}

object VersionData {
  trait Builder {
    var product:Product = null
    def product( value:Product ):this.type = {
      product = value
      this
    }

    var name:String = null
    def name( value:String ):this.type = {
      name = value
      this
    }

    var comment:String = ""
    def comment( value:String ):this.type = {
      comment = value
      this
    }

    def build:VersionData
  }
}
