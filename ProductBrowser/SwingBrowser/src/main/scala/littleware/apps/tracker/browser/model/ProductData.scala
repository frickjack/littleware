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

import littleware.base.ValidationException

trait ProductData {
  def  parentPath:String
  def  name:String
  def  comment:String
}

object ProductData {
  trait Builder {
    def  parentPath:String
    def  parentPath( value:String ):this.type
    def  name:String
    def  name( value:String ):this.type
    def  comment:String
    def  comment( value:String ):this.type
    @throws(classOf[ValidationException])
    def  build:ProductData
  }
}