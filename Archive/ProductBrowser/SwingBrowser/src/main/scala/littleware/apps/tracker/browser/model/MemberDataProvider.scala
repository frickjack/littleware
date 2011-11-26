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
import littleware.apps.tracker.Version
import littleware.base.validate.ValidationException
import littleware.base.Whatever


object MemberDataProvider extends Provider[MemberData.Builder] {

  class SimpleData (
    override val version:Version,
    override val name:String,
    override val comment:String,
    override val data:String
  ) extends MemberData {}

  class SimpleBuilder extends MemberData.Builder {
    override def build = {
      if ( Option( version ).isEmpty ) {
        throw new ValidationException( "Version must be set for MemberData" )
      }
      if ( Whatever.get.empty( name ) ) {
        throw new ValidationException( "Member name not specified")
      }
      new SimpleData( version, name, comment, data )
    }
  }

  override def get = new SimpleBuilder

}
