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

import com.google.inject.{Inject,Provider}
import java.net.URL
import java.util.logging.Level
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import littleware.apps.littleId.server._
import littleware.scala.LazyLogger
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap
import scala.collection.JavaConversions._

object ProviderRespServlet {
  @Inject
  class Tools(
    val openIdTool:controller.OpenIdTool,
    val responseBuilder:Provider[model.AuthResponse.Builder]
  ) {}

  /**
   * Data for client-response jsp to work with
   */
  case class ClientResponseBean(
    val authSuccess:Boolean,
    val email:String,
    val openId:String,
    val verifySecret:String
  ) {
  }

  val clientResponseBeanKey = "clientResponseBean"
}

import ProviderRespServlet._


/**
 * Servlet handles the response from the OId provider,
 * assembles a model.AuthResponse request-scope bean,
 * and forwards to an authResponse jsp that generates the
 * client response.
 *
 * @TODO implement InjectMeServlet abstract base class
 */
class ProviderRespServlet extends HttpServlet {
  private val log = LazyLogger( getClass )

  var clientResponsePage:String = "/en/openId/respondToClient.jsp"
  private var tools:Tools = null

  @Inject
  def injectMe( tools:Tools ):Unit = {
    this.tools = tools
  }

  /**
   * Lookup the GuiceBean, and self-inject.
   * Also processes the "providerSubmitForm"
   */
  @throws(classOf[ServletException])
  override def init():Unit = try {
    Option( getServletConfig.getInitParameter( "clientResponsePage" ) ).map( (value) => { clientResponsePage = value })
    val gbean:GuiceBean = getServletContext.getAttribute( WebBootstrap.littleGuice ).asInstanceOf[GuiceBean]
    gbean.injectMembers(this)
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Initialization failed", ex )
        ex match {
          case _:ServletException => throw ex
          case _ => throw new ServletException( "Initialization failed", ex )
        }
      }
  }

  @throws(classOf[ServletException])
  def doGetOrPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = {
    val session = req.getSession
    val oIdRequestData:controller.OpenIdTool.OIdRequestData = session.getAttribute( AuthReqServlet.oIdRequestDataKey ).asInstanceOf
    val authRequest:model.AuthRequest = session.getAttribute( AuthReqServlet.authRequestKey ).asInstanceOf
    require( oIdRequestData != null, "OId request data is stored with session" )
    require( authRequest != null, "Client authRequest data is stored with session")
    val response:model.AuthResponse = tools.openIdTool.processResponse(oIdRequestData, new URL( req.getRequestURL.toString ),
                                                                       req.getParameterMap.entrySet.map( (entry) => (entry.getKey -> entry.getValue)
      ).toMap
    ) match {
      case Some(creds) => tools.responseBuilder.get.request( authRequest ).success( creds )
      case _ => tools.responseBuilder.get.request( authRequest ).failure
    }
    val responseBean = response match {
      case success:model.AuthResponse.AuthSuccess =>
        ClientResponseBean( true, success.userInfo.email,
                           success.userInfo.openId.toString,
                           success.verifySecret
        )
      case _ => ClientResponseBean( false, "authorization failed", "authorization failed", "authorization failed")
    }

    req.setAttribute( clientResponseBeanKey, responseBean )
    req.getRequestDispatcher( clientResponsePage ).forward(req,resp)
  }

  @throws(classOf[ServletException])
  override def doPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
  @throws(classOf[ServletException])
  override def doGet( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )

}
