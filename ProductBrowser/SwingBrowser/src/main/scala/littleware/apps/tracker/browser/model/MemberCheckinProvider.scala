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
import littleware.base.ValidationException
import littleware.base.Whatever
import java.io.File

object MemberCheckinProvider extends Provider[MemberCheckinData.Builder] {

  class Data(
    override val version:Version,
    override val name:String,
    override val comment:String,
    override val dataDir:File
    ) extends MemberCheckinData {}

  class Builder extends MemberCheckinData.Builder {
    override def build = {
      if ( null == version ) {
        throw new ValidationException( "Must specify version" )
      }
      if ( Whatever.get.empty( name ) ) {
        throw new ValidationException( "Invalid member name: " + name )
      }
      if ( ! dataDir.exists ) {
        throw new ValidationException( "No data at " + dataDir )
      }
      new Data( version, name, comment, dataDir )
    }
  }

  override def get = new Builder
}
