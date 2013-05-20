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
package internal

import com.google.gson
import com.google.inject
import java.net.URL
import java.util.Properties
import java.util.logging.Level
import littleware.apps.littleId
import littleware.apps.littleId.server.controller
import littleware.base.PropertiesLoader
import littleware.web.jwt
import java.util.logging.{Level,Logger}
import org.openid4java.consumer
import org.openid4java.discovery
import org.openid4java.message
import scala.collection.JavaConversions._


class SimpleOidTool @inject.Inject() (
  consumerMgr:consumer.ConsumerManager,
  userCredsFactory:inject.Provider[common.model.OIdUserCreds.Builder],
  requestFactory:inject.Provider[model.AuthRequest.Builder],
  tokenFoundry:jwt.TokenFoundry,
  gsonTool:gson.Gson
) extends controller.OpenIdTool {
  private val log = Logger.getLogger( getClass.getName )
  private val props:Properties = PropertiesLoader.get.loadProperties( classOf[controller.OpenIdTool] )

  log.log( Level.INFO, "Registering OpenID consumer URL: " + props.getProperty( "consumerURL" ) )
  override var consumerURL = new URL( props.getProperty( "consumerURL" ) )

  private def associateWithProvider( oidProvider:littleId.common.model.OIdProvider.Value ):discovery.DiscoveryInformation = {
    val openid:URL = oidProvider match {
      case littleId.common.model.OIdProvider.Yahoo => new URL( "https://me.yahoo.com" )
      case _ => new URL( "https://www.google.com/accounts/o8/id" )
    }

    // attempt to associate with an OpenID provider
    // and retrieve one service endpoint for authentication
    consumerMgr.associate( consumerMgr.discover( openid.toString ) )
  }

  def startOpenIdAuth( authReq:model.AuthRequest ):model.DataForProvider = {
    val discovered = associateWithProvider( authReq.openIdProvider )

    // store the discovery information in the user's session
    //session.setAttribute("openid-disco", discovered)

    // obtain a AuthRequest message to be sent to the OpenID provider
    val authMess:message.AuthRequest = consumerMgr.authenticate( 
      // put the name of the provider at the end of the URL: /google, /yahoo, whatever
      discovered, consumerURL.toString.replaceAll( "/+$", "" ) + "/" + authReq.openIdProvider 
    );
    {
      // Attribute Exchange example: fetching the 'email' attribute
      val fetch = message.ax.FetchRequest.createFetchRequest()
      fetch.addAttribute("email",
                         // attribute alias
                         "http://axschema.org/contact/email",
                         //"http://schema.openid.net/contact/email",   // type URI
                         true)                                      // required

      fetch.setCount( "email", 1 );
      // attach the extension to the authentication request
      authMess.addExtension(fetch)
    }
    model.DataForProvider( authReq,
                      new URL( authMess.getOPEndpoint() ),
                      authMess.getParameterMap.entrySet.map(
        (entry) => entry.getKey.toString -> entry.getValue.toString
      ).toMap
    )
  }
  

  override def processProviderResponse( oidProvider:littleId.common.model.OIdProvider.Value,
                      consumerEndpoint:URL, responseParams:Map[String,Array[String]]
  ):Option[littleId.common.model.OIdUserCreds] = {
    val discovered = associateWithProvider( oidProvider )
    val userBuilder = userCredsFactory.get()
    
    // Verify the validity of the response
    consumerMgr.verify(
      consumerEndpoint.toString(),
      new message.ParameterList( responseParams ),
      discovered
    ).getAuthResponse match {
      case authSuccess:message.AuthSuccess => {
          val openId:String = authSuccess.getIdentity
          val maybeEmail:Option[String] = authSuccess.getExtension( message.ax.AxMessage.OPENID_NS_AX ) match {
                case fetchResp:message.ax.FetchResponse => Some( fetchResp.getAttributeValue("email") )
                case _ => {
                    log.info( "Got openId response with no e-mail data ...")
                    None
                }
              }
          maybeEmail.map( (email) => userBuilder.email( email ).openId( new URL( openId ) ).build )
        }
      case _ => None
    }
  }
  
  def credsToToken( creds:common.model.OIdUserCreds ):String =
    tokenFoundry.makeToken( gsonTool.toJsonTree( creds, classOf[common.model.OIdUserCreds] ).getAsJsonObject )
}
