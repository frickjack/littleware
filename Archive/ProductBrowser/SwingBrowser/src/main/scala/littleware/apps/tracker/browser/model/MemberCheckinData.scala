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

import java.io.File
import littleware.apps.tracker.Version

trait MemberCheckinData {
  val   version:Version
  val   name:String
  val   comment:String
  val   dataDir:File
}

object MemberCheckinData {
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
    var comment:String = null
    def comment( value:String ):this.type = {
      comment = value
      this
    }
    var dataDir:File = null
    def dataDir( value:File ):this.type = {
      dataDir = value
      this
    }

    def build:MemberCheckinData
  }
}