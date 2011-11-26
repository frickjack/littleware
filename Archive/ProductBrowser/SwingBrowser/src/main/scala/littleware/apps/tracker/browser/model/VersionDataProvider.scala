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
import littleware.apps.tracker.Product
import littleware.base.validate.ValidationException
import littleware.base.Whatever

object VersionDataProvider extends Provider[VersionData.Builder] {

  class SimpleData(
    override val product:Product,
    override val name:String,
    override val comment:String
  ) extends VersionData {}

  class SimpleBuilder extends VersionData.Builder {
    override def build = {
      if ( Option( product ).isEmpty ) {
        throw new ValidationException( "Product must be set for VersionData" )
      }
      if ( Whatever.get.empty( name ) ) {
        throw new ValidationException( "Version name not specified")
      }
      new SimpleData( product, name, comment )
    }
  }

  override def get:VersionData.Builder = new SimpleBuilder
}
