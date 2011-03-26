/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.common.model

import java.net.URL
import littleware.scala.LittleHelper
import littleware.scala.LittleValidator


/**
 * UserCredentials guaranteed to have e-mail and open-id
 */
trait OIdUserCreds extends UserCreds {
  val email:String
  val openId:URL
}

object OIdUserCreds {
  trait Builder extends LittleValidator {
    var email:String = null
    def email( value:String ):this.type = {
      this.email = value
      this
    }

    var openId:URL = null
    def openId( value:URL ):this.type = {
      this.openId = value
      this
    }

    override def checkSanity():Seq[String] = LittleValidator.helper.
          check( LittleHelper.emptyCheck( email ).isDefined, "Email defined"
                ).check( null != openId, "OpenId URL defined"
                ).errors

    def build():OIdUserCreds
  }
}