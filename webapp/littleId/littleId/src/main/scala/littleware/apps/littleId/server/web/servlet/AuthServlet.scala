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
import java.util.logging.Level
import javax.naming.InitialContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import littleware.apps.littleId.server._
import littleware.bootstrap.client.AppBootstrap
import littleware.scala.LazyLogger
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap

class AuthServlet extends HttpServlet {
  private val log = LazyLogger( getClass )

  @Inject
  class Tools(
    val openIdTool:controller.OpenIdTool,
    val responseBuilder:Provider[model.AuthResponse.Builder]
  ) {}

  private var tools:Tools = null

  @Inject
  def injectMe( tools:Tools
  ):Unit = {
    this.tools = tools
  }

  /**
   * Lookup the GuiceBean, and self-inject
   */
  @throws(classOf[ServletException])
  override def init():Unit = try {
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
}
