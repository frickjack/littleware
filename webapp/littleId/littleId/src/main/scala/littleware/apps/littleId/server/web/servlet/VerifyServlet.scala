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
import littleware.apps.littleId
import littleId.server._
import littleware.scala.LazyLogger
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap
import scala.collection.JavaConversions._

object VerifyServlet {
  @Inject
  class Tools(
    val verifyTool:controller.AuthVerifyTool,
    val userBuilder:Provider[littleId.OIdUserCreds.Builder]
  ) {}


  val verifyResponseBeanKey = "verifyResponseBean"
}

import VerifyServlet._


/**
 * Servlet handles the response from the OId provider,
 * assembles a model.AuthResponse request-scope bean,
 * and forwards to an authResponse jsp that generates the
 * client response.
 *
 * @TODO implement InjectMeServlet abstract base class
 */
class VerifyServlet extends HttpServlet {
  private val log = LazyLogger( getClass )

  var verifyResponsePage:String = "/openId/view/en/verifyResponse.jsp"
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
    Option( getServletConfig.getInitParameter( "verifyResponsePage" ) ).map( (value) => { verifyResponsePage = value })
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
    val secret:String = Option( req.getParameter( "secret" ) ).getOrElse( "" )
    val userCreds:littleId.OIdUserCreds = tools.userBuilder.get.email(
      Option( req.getParameter( "email" ) ).getOrElse( "" )
      ).openId(
      Option( req.getParameter( "openId" ) ).map( (id) => new URL( id ) ).getOrElse( new URL( "http://openId/not/provided/"))
    ).build
    req.setAttribute( "verifyRequest", model.VerifyRequest( userCreds, secret ) )
    req.setAttribute( "verifyResponse", model.VerifyResponse( tools.verifyTool.verifyCreds(secret, userCreds) )  )
    req.getRequestDispatcher( verifyResponsePage ).forward(req,resp)
  }

  @throws(classOf[ServletException])
  override def doPost( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
  @throws(classOf[ServletException])
  override def doGet( req:HttpServletRequest, resp:HttpServletResponse ):Unit = doGetOrPost( req, resp )
}
