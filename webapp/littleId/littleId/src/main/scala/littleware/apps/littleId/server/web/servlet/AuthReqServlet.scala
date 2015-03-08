/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.web.servlet

import com.google.gson
import com.google.inject.Inject
import com.google.inject.Provider
import java.net.URL
import java.util.logging.Level
import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import littleware.apps.littleId.common.model.OIdProvider
import littleware.apps.littleId.server._
import java.util.logging.{Level,Logger}
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap
import littleware.web.servlet.helper.JsonResponse
import littleware.web.servlet.helper.ResponseHelper
import scala.collection.JavaConversions._

object AuthReqServlet {
  
  class Tools @Inject() (
    val openIdTool:controller.OpenIdTool,
    val jsResponseFactory:Provider[JsonResponse.Builder],
    val helper:ResponseHelper,
    val gsonTool:gson.Gson,
    val requestFactory:Provider[model.AuthRequest.Builder]
  ) {}



  /**
   * Session attribute key where OIdRequestData is stored
   */
  val oIdRequestDataKey = "oIdRequestData"
  /**
   * Session attribute key where authRequest is stored
   */
  val authRequestKey = "authRequest"
}

import AuthReqServlet._

/**
 * Servlet handles client auth-request.
 * Either doGet or doPost unpacks the "provider" param,
 * discovers corresponsing OpenID provider,
 * bundles up a FormDataBean with the endpoint and paramerters for the provider,
 * and returns to the client in json form, so client can
 * post the request to the OpenId provider.
 */
class AuthReqServlet extends HttpServlet {
  private val log = Logger.getLogger( getClass.getName )
  private var tools:Tools = null

  @Inject
  def injectMe( tools:Tools
  ):Unit = {
    this.tools = tools
  }


  /**
   * Lookup the GuiceBean, and inject dependencies.
   */
  @throws(classOf[ServletException])
  override def init():Unit = try {
    val gbean:GuiceBean = getServletContext.getAttribute( WebBootstrap.littleGuice ).asInstanceOf[GuiceBean]
    assert( gbean != null, "Able to acquire Guice bean from app environment" )
    gbean.injectMembers(this)
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "AuthServlet initialization failed", ex )
        ex match {
          case _:ServletException => throw ex
          case _ => throw new ServletException( "AuthServlet initialization failed", ex )
        }
      }
  }

  @throws(classOf[ServletException])
  def doGetOrPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = {
    val authRequest:model.AuthRequest = {
      val provider = Option( req.getParameter( "provider" ) ).getOrElse( "google" ).toLowerCase match {
        case "yahoo" => OIdProvider.Yahoo
        case _ => OIdProvider.Google
      }
      tools.requestFactory.get.openIdProvider( provider 
        ).replyToURL( new java.net.URL( req.getParameter( "replyTo" ) ) 
        ).build() 
    }
    
    val dataForProvider = tools.openIdTool.startOpenIdAuth( authRequest )
    
    // set a cookie with the AuthResponse state
    val state = model.AuthState.Running( dataForProvider.request )
    val cookie = new Cookie( controller.OpenIdTool.stateCookieName, 
                            tools.gsonTool.toJson( state, classOf[model.AuthState]) 
      )
    cookie.setMaxAge( 300 ) // 5 minutes
    cookie.setPath( Option( req.getContextPath ).filter( _.nonEmpty ).getOrElse( "/" ) )
    resp.addCookie( cookie )
    
    val jsonResponse = tools.jsResponseFactory.get.content.set( 
      tools.gsonTool.toJsonTree( dataForProvider, classOf[model.DataForProvider] ).getAsJsonObject
    ).build();
    tools.helper.write( resp, jsonResponse )
  }

  @throws(classOf[ServletException])
  override def doPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
  @throws(classOf[ServletException])
  override def doGet( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
}
