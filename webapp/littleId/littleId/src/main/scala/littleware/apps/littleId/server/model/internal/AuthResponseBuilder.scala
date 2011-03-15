/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.model.internal

import littleware.apps.littleId.server.model
import littleware.apps.littleId
import littleware.base.validate.ValidatorUtil
import littleware.scala.LittleHelper

class AuthResponseBuilder extends model.AuthResponse.Builder {
  private case class Success(
    @scala.reflect.BeanProperty
    request:model.AuthRequest,
    @scala.reflect.BeanProperty
    userInfo:littleId.OIdUserCreds,
    @scala.reflect.BeanProperty
    verifySecret:String
  ) extends model.AuthResponse.AuthSuccess {}

  private case class Failure(
    @scala.reflect.BeanProperty
    request:model.AuthRequest
  ) extends model.AuthResponse.AuthFailure {}

  private class Step2(
    val request:model.AuthRequest
  ) extends model.AuthResponse.BuildStep2 {
    require( null != request, "AuthResponseBuilder request may not be null" )

    override def success( userInfo:littleId.OIdUserCreds, verifySecret:String ):model.AuthResponse.AuthSuccess = {
      require( null != userInfo, "userInfo may not be null")
      require( LittleHelper.emptyCheck( verifySecret ).isDefined, "verifySecret may not be empty" )
      Success( request, userInfo, verifySecret )
    }

    override def failure():model.AuthResponse.AuthFailure = Failure( request )
  }

  override def request( request:model.AuthRequest ):model.AuthResponse.BuildStep2 = new Step2( request )
}
