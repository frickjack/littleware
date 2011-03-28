/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId.client.web.bean

import java.security.Principal
import littleware.apps.littleId.common.model.UserCreds

/**
 * Bean injected into the HTTP Session by the servlet.LoginHandler
 */
case class IdBean (
  @reflect.BeanProperty
  user:Principal,
  @reflect.BeanProperty
  isGuest:Boolean
  ) extends java.io.Serializable {

  /**
   * NOOP constructor == IdBean( "guest@guest", true )
   */
  def this() = this( UserCreds( "guest@guest" ), true )

  /**
   * User constructor == IdBean( user, false )
   */
  def this( user:Principal ) = this( user, false )
}
