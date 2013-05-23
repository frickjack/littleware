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
package web
package servlet


import com.google.gson
import com.google.inject
import java.net.URL
import java.util.logging.Level
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.logging.{Level,Logger}
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap
import littleware.web.servlet.helper.JsonResponse
import littleware.web.servlet.helper.ResponseHelper
import scala.collection.JavaConversions._

object VerifyServlet {
  
  class Tools @inject.Inject() (
    val verifyTool:client.controller.internal.InMemoryVerifyTool,
    val gsonTool:gson.Gson,
    val jsResponseFactory:inject.Provider[JsonResponse.Builder],
    val responseHelper:ResponseHelper
  ) {}


  val verifyResponseBeanKey = "verifyResponseBean"
}

import VerifyServlet._


/**
 * Servlet verifies that either the submitted token or
 * the token from the client's littleId cookie if not supplied
 * is a valid token.
 */
class VerifyServlet extends HttpServlet {
  private val log = Logger.getLogger( getClass.getName )

  private var tools:Tools = null

  @inject.Inject
  def injectMe( tools:Tools ):Unit = {
    this.tools = tools
  }

  /**
   * Lookup the GuiceBean, and self-inject.
   * Also processes the "providerSubmitForm"
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
    val optSecret:Option[String] = Option( req.getParameter( "secret" ) ) match {
      case Some(token) => Some(token)
      case _ => req.getCookies.find( _.getName == controller.OpenIdTool.stateCookieName 
        ).flatMap( 
          cookie => tools.gsonTool.fromJson( cookie.getValue, classOf[model.AuthState]) match {
            case model.AuthState.Success( _, _, token) => Some(token)
            case _ => None
          }
        )
    }

    val jsResponse:JsonResponse =  optSecret.flatMap( secret => {
      tools.verifyTool.verify( secret ).map( 
        (creds:common.model.OIdUserCreds) => 
          tools.jsResponseFactory.get.content.set( 
            tools.gsonTool.toJsonTree( creds, classOf[common.model.OIdUserCreds] ).getAsJsonObject 
          ).build 
      )
    }).getOrElse( 
      // either no secret token was passed as a paramter or cookie,
      // or the given token failed validation
      tools.jsResponseFactory.get.status.set( HttpServletResponse.SC_NOT_FOUND ).build() 
    )
  
    tools.responseHelper.write( resp, jsResponse )
  }

  @throws(classOf[ServletException])
  override def doPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
  @throws(classOf[ServletException])
  override def doGet( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
}
