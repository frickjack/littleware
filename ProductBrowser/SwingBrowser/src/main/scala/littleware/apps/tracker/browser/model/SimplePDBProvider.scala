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
import edu.auburn.library.util.JclWrapper._
import littleware.base.ValidationException

object SimplePDBProvider extends Provider[ProductData.Builder] {

  class Builder extends ProductData.Builder {
    var parentPath = ""
    override def parentPath( value:String ):this.type = {
      parentPath = value
      this
    }
    var name = ""
    override def name( value:String ):this.type = {
      name = value
      this
    }
    var comment = ""
    override def comment( value:String ):this.type = {
      comment = value
      this
    }

    @throws(classOf[ValidationException])
    override def build = {
      new Data(
        emptyCheck( parentPath ) match {
          case Some(x) => x
          case _ => throw new ValidationException( "Empty product parent")
        },
        emptyCheck( name  ) match {
          case Some(x) => x
          case _ => throw new ValidationException( "Empty name" )
        },
        comment
      )
    }
  }

  class Data( val parentPath:String, val name:String, val comment:String ) extends ProductData {}

  override def get = new Builder
}
