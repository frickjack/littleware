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
import com.google.inject.{Inject,Provider}
import java.net.URL
import java.net.URLEncoder
import java.util.logging.Level
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import littleware.apps.littleId._
import littleware.apps.littleId.server._
import java.util.logging.{Level,Logger}
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap
import scala.collection.JavaConversions._

object ProviderRespServlet {
  
  class Tools @Inject() (
    val openIdTool:controller.OpenIdTool,
    val gsonTool:gson.Gson
  ) {}

  /**
   * Data for client-response jsp to work with
   */
  case class ClientResponseBean(
    @reflect.BeanProperty
    val authSuccess:Boolean,
    @reflect.BeanProperty
    val email:String,
    @reflect.BeanProperty
    val openId:String,
    @reflect.BeanProperty
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
  private val log = Logger.getLogger( getClass.getName )

  private var tools:Tools = null

  @Inject
  def injectMe( tools:Tools ):Unit = {
    this.tools = tools
  }

  /**
   * Lookup the GuiceBean, and inject dependencies.
   */
  @throws(classOf[ServletException])
  override def init():Unit = try {
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
    val oidProvider = {
      // Request URL should end in /google or /yahoo or whatever
      val uriStr = req.getRequestURI()
      val providerName:String = uriStr.substring( uriStr.lastIndexOf( "/" ) + 1 )
      common.model.OIdProvider.withName( providerName )
    }
    
    val authRequest:model.AuthRequest = {
      val cookie = req.getCookies().find( _.getName() == controller.OpenIdTool.stateCookieName ).get
      val state = tools.gsonTool.fromJson( cookie.getValue, classOf[model.AuthState] )
      state.request
    }
    
    val authState:model.AuthState = tools.openIdTool.processProviderResponse(
      oidProvider, new URL( req.getRequestURL.toString ),
      req.getParameterMap.entrySet.map( (entry) => (entry.getKey -> entry.getValue)
      ).toMap
    ).map( (creds) => model.AuthState.Success( authRequest, creds, tools.openIdTool.credsToToken(creds) )
    ).getOrElse( model.AuthState.Failure( authRequest ))

    //
    // store the response in a cookie, then send a page back to the
    // client to close the login popup (javascript API puts user in
    // a popup for OpenId provider (Google, Yahoo, whatever) )
    // 
    val jsStr = tools.gsonTool.toJson( authState, classOf[model.AuthState] )
    val cookie = new javax.servlet.http.Cookie( controller.OpenIdTool.stateCookieName, jsStr )
    cookie.setMaxAge( 300 )
    cookie.setPath( Option( req.getContextPath ).filter( _.nonEmpty ).getOrElse( "/" ) )
    resp.addCookie( cookie )
    
    //
    // redirect to replyTo page with openId parameters added to the url string -
    // maybe add getCreds call later that just returns the cookie contents ?
    // 
    val urlStr = authState match {
      case model.AuthState.Failure(_) => authRequest.replyToURL.toString + "?authSuccess=false"
      case success:model.AuthState.Success =>
        authRequest.replyToURL.toString + "?authSuccess=true" +
          "&email=" + URLEncoder.encode( success.userInfo.email, "UTF8" ) +
          "&openId=" + URLEncoder.encode( success.userInfo.openId.toString, "UTF8" ) +
          "&verifySecret=" + URLEncoder.encode( success.verifySecret, "UTF8" )
    }
    resp.sendRedirect( resp.encodeRedirectURL( urlStr ) );
  }

  @throws(classOf[ServletException])
  override def doPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
  @throws(classOf[ServletException])
  override def doGet( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )

}
