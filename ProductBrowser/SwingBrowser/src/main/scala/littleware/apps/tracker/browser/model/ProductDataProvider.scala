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
import littleware.asset.AssetPath
import littleware.base.ValidationException
import littleware.scala.LittleHelper

object ProductDataProvider extends Provider[ProductData.Builder] {

  class Builder extends ProductData.Builder {
    @throws(classOf[ValidationException])
    override def build = {
      new Data(
        Option( parentPath ).getOrElse(
          { throw new ValidationException( "Empty product parent") }
        ),
        LittleHelper.emptyCheck( name  ).getOrElse(
          { throw new ValidationException( "Empty name" ) }
        ),
        comment
      )
    }
  }

  class Data( val parentPath:AssetPath, val name:String, val comment:String ) extends ProductData {}

  override def get = new Builder
}
