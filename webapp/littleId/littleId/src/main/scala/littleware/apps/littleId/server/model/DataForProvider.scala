/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId.server.model

import java.net.URL
import java.util.UUID

/**
 * Data returned to the client by an authorization request -
 * the client then posts these parameters to the openId provider
 * in a popup.  Assuming the user approves the openId request,
 * then the provider in turn posts credentials back to our littleId server,
 * which makes an AuthResponse data available to the client.
 */
case class DataForProvider (
  /**
   * The request this data is associated with
   */
  request:AuthRequest,
  /** provider endpoint to which to route the user for authentication */
  providerEndpoint:URL,
  /** HTTP parameters to deliver to the provider endpoint */
  params:Map[String,String]  
) {
}
