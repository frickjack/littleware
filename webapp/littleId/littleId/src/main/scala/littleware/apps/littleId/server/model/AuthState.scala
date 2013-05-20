/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId
package server
package model


abstract class AuthState {
  val request:AuthRequest
}

object AuthState {
  /** Still wating for response from provider */
  case class Running ( request:AuthRequest ) extends AuthState {}
  
  case class Success( request:AuthRequest,
                         userInfo:common.model.OIdUserCreds,
                         verifySecret:String
    ) extends AuthState

  case class Failure ( request:AuthRequest ) extends AuthState {}

}