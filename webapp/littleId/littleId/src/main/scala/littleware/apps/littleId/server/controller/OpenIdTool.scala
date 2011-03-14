/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.controller

import java.net.URL
import littleware.apps.littleId
import org.openid4java.discovery.DiscoveryInformation

object OpenIdTool {
  /**
   * POJO bundles data needed to route the client to the OID provider for authentication
   */
  trait OIdRequestData {
    /** providerInfo discovery information needed to by openid engine to process response */
    val providerInfo:DiscoveryInformation
    /** provider endpoint to which to route the user for authentication */
    val providerEndpoint:URL
    /** HTTP parameters to deliver to the provider endpoint */
    val params:Map[String,String]
  }
}

import OpenIdTool._

trait OpenIdTool {
  /**
   * Assemble the URL and POST parameters to send to the OpenID provider
   * to request user authentication
   *
   * @param oidProvider to build request data for
   * @return data needed to route the client over to the OId provider
   */
  def buildRequest( oidProvider:littleId.OIdProvider.Value ):OIdRequestData

  /**
   * Process the authentication response from the OID provider
   *
   * @param requestData that the response is responding to
   * @param consumerEndpoint URL that the response was received at
   * @param responseParams HTTP parameters delivered by the response
   * @return credentials for the authenticated user - or none if no user authenticated
   */
  def processResponse( requestData:OIdRequestData, consumerEndpoint:URL, responseParams:Map[String,Array[String]] ):Option[littleId.OIdUserCreds]
}
