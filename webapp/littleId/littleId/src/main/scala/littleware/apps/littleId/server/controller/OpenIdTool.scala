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
package controller

import com.google.gson
import java.net.URL
import littleware.apps.littleId


/**
 * Tool for interacting with an OId provider
 */
trait OpenIdTool {
  
  /**
   * Property specifies the URL where the server's OpenID consumer
   * (Provider Response Servlet) is waiting for the OpenID client redirect - ex:
   *      http://beta.frickjack.com:8080/openId/services/providerResponse/
   */
  var consumerURL:java.net.URL
  
  /**
   * Assemble the URL and POST parameters to send to the OpenID provider
   * to request user authentication
   *
   * @param oidProvider to build request data for
   * @return data needed to route the client over to the OId provider
   */
  def startOpenIdAuth( authReq:model.AuthRequest ):model.DataForProvider
  

  /**
   * Process the authentication response from the OID provider -
   * delivered to us when the provider redirects the client back
   * to our server response end point.
   *
   * @param requestData that the response is responding to
   * @param consumerEndpoint URL that the response was received at
   * @param responseParams HTTP parameters delivered by the response
   * @return credentials for the authenticated user - or none if no user authenticated
   */
  def processProviderResponse( oidProvider:littleId.common.model.OIdProvider.Value, 
                              consumerEndpoint:URL, 
                              responseParams:Map[String,Array[String]] 
  ):Option[littleId.common.model.OIdUserCreds]
  
  /**
   * Encode the given credenties as a signed web token that trusted partners
   * can verify
   */
  def credsToToken( creds:littleId.common.model.OIdUserCreds ):String
}

object OpenIdTool {
  val stateCookieName = "littleIdState"
}