/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.model

/**
 * Data backing AuthResponse interaction
 */
import java.util.UUID
import littleware.apps.littleId.common.model.OIdUserCreds

trait AuthResponse {
  val request:AuthRequest
}

object AuthResponse {
  trait AuthSuccess extends AuthResponse {
    val userInfo:OIdUserCreds
    /**
     * Secret that client can contact server with to verify response data.
     * Must contact within 5 minutes - 1-time verification
     */
    val verifySecret:String
  }

  trait AuthFailure extends AuthResponse {}



  trait Builder {
    def request( value:AuthRequest ):BuildStep2
  }
  
  trait BuildStep2 {
    val request:AuthRequest
    def success( userInfo:OIdUserCreds, verifySecret:String ):AuthSuccess
    /**
     * Calls 2-arg success with random UUID secret
     */
    def success( userInfo:OIdUserCreds ):AuthSuccess = success( userInfo, UUID.randomUUID.toString )
    def failure():AuthFailure
  }
  
}