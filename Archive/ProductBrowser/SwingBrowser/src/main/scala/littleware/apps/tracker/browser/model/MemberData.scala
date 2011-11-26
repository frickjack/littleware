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

import littleware.apps.tracker.Version

trait MemberData {
  val version:Version
  val name:String
  val comment:String
  val data:String
}

object MemberData {
  trait Builder {
    var version:Version = null
    def version( value:Version ):this.type = {
      version = value
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

    var data:String = ""
    def data( value:String ):this.type = {
      data = value
      this
    }
    
    def build:MemberData
  }
}
