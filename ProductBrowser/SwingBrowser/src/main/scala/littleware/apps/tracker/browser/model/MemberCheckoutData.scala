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
import littleware.apps.tracker.Member

trait MemberCheckoutData {
  val member:Member
  val destinationDir:File
}

object MemberCheckoutData {
  trait Builder {
    var member:Member = null
    def member( value:Member ):this.type = {
      member = value
      this
    }

    var destinationDir:File = null
    def destinationDir( value:File ):this.type = {
      destinationDir = value
      this
    }

    def build:MemberCheckoutData
  }
}
