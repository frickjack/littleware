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
import java.io.File
import littleware.apps.tracker.Member
import littleware.base.ValidationException


object MemberCheckoutProvider extends Provider[MemberCheckoutData.Builder] {

  class Data (
    override val member:Member,
    override val destinationDir:File
    ) extends MemberCheckoutData {}

  class Builder extends MemberCheckoutData.Builder {
    override def build:MemberCheckoutData = {
      if ( null == member ) {
        throw new ValidationException( "Member for checkout not specified" )
      }
      if ( null == destinationDir ) {
        throw new ValidationException( "Must specify destination for checkout")
      }
      new Data( member, destinationDir )
    }
  }

  def get = new Builder()
}
