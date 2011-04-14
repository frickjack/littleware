/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.controller.internal

import com.google.inject.Inject
import java.net.URL
import java.util.Properties
import littleware.apps.littleId
import littleware.apps.littleId.server.controller
import littleware.base.PropertiesLoader
import littleware.scala.LazyLogger
import org.openid4java.consumer
import org.openid4java.discovery
import org.openid4java.message
import scala.collection.JavaConversions._


class SimpleOidTool @Inject() (
  consumerMgr:consumer.ConsumerManager,
  userBuilder:littleId.common.model.OIdUserCreds.Builder
) extends controller.OpenIdTool {
  private val log = LazyLogger( getClass )
  private val props:Properties = PropertiesLoader.get.loadProperties( classOf[controller.OpenIdTool] )

  private case class SimpleRequestData(
    override val providerInfo:discovery.DiscoveryInformation,
    override val providerEndpoint:URL,
    override val params:Map[String,String]
  ) extends controller.OpenIdTool.OIdRequestData {}
  
  val consumerURL = new URL( props.getProperty( "consumerURL" ) )

  def buildRequest( oidProvider:littleId.common.model.OIdProvider.Value ):controller.OpenIdTool.OIdRequestData = {
    val openid:URL = oidProvider match {
      case littleId.common.model.OIdProvider.Yahoo => new URL( "https://me.yahoo.com" )
      case _ => new URL( "https://www.google.com/accounts/o8/id" )
    }

    // attempt to associate with an OpenID provider
    // and retrieve one service endpoint for authentication
    val discovered:discovery.DiscoveryInformation = consumerMgr.associate( consumerMgr.discover( openid.toString ) )

    // store the discovery information in the user's session
    //session.setAttribute("openid-disco", discovered)

    // obtain a AuthRequest message to be sent to the OpenID provider
    val authReq:message.AuthRequest = consumerMgr.authenticate( discovered, consumerURL.toString );
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
      authReq.addExtension(fetch)
    }
    SimpleRequestData( discovered,
                      new URL( authReq.getOPEndpoint() ),
                      authReq.getParameterMap.entrySet.map(
        (entry) => entry.getKey.toString -> entry.getValue.toString
      ).toMap
    )
  }

  def processResponse( requestData:controller.OpenIdTool.OIdRequestData, 
                      consumerEndpoint:URL, responseParams:Map[String,Array[String]]
  ):Option[littleId.common.model.OIdUserCreds] = {
    // Verify the validity of the response
    consumerMgr.verify(
      consumerEndpoint.toString(),
      new message.ParameterList( responseParams ),
      requestData.providerInfo
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
}
