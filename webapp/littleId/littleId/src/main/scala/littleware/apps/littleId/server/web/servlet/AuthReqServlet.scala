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

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Provider
import java.net.URL
import java.util.UUID
import java.util.logging.Level
import javax.naming.InitialContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import littleware.apps.littleId.OIdProvider
import littleware.apps.littleId.server._
import littleware.bootstrap.client.AppBootstrap
import littleware.scala.LazyLogger
import littleware.scala.LittleHelper
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap
import scala.collection.JavaConversions._

object AuthReqServlet {
  @Inject
  class Tools(
    val openIdTool:controller.OpenIdTool
  ) {}

  /**
   * Little bean passed to jsp that posts a request-data form to
   * the OpenId provider
   */
  class FormDataBean (
    @scala.reflect.BeanProperty
    val actionURL:URL,
    @scala.reflect.BeanProperty
    val params:java.util.Map[String,String]
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
 * and forwards the request to a .jsp to construct and auto-submit the provider data form.
 */
class AuthReqServlet extends HttpServlet {
  private val log = LazyLogger( getClass )


  private var tools:Tools = null

  @Inject
  def injectMe( tools:Tools
  ):Unit = {
    this.tools = tools
  }

  /**
   * Property determines the context-relative URL of the .jsp
   * that builds and submits the provider-data form using the FormDataBean
   * published by this servlet's doGet or doPost methods
   */
  var providerSubmitForm = "/openId/view/en/postToProvider.jsp"

  /**
   * Lookup the GuiceBean, and self-inject.
   * Also processes the "providerSubmitForm"
   */
  @throws(classOf[ServletException])
  override def init():Unit = try {
    Option( getServletConfig.getInitParameter( "providerSubmitForm" ) ).map( (value) => { providerSubmitForm = value })
    val gbean:GuiceBean = getServletContext.getAttribute( WebBootstrap.littleGuice ).asInstanceOf[GuiceBean]
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
    val provider:OIdProvider.Value = LittleHelper.emptyCheck( req.getParameter( "provider" ) ).getOrElse( "google" )toLowerCase match {
      case "yahoo" => OIdProvider.Yahoo
      case _ => OIdProvider.Google
    }
    val authReq:model.AuthRequest = model.AuthRequest( provider )
    val oidReq:controller.OpenIdTool.OIdRequestData = tools.openIdTool.buildRequest(provider)
    val session = req.getSession
    session.setAttribute( oIdRequestDataKey, oidReq )
    session.setAttribute( authRequestKey, authReq )
    req.setAttribute( "formDataBean", new FormDataBean( oidReq.providerEndpoint, oidReq.params ) )
    req.getRequestDispatcher( providerSubmitForm ).forward( req, resp )
  }

  @throws(classOf[ServletException])
  override def doPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
  @throws(classOf[ServletException])
  override def doGet( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
}
