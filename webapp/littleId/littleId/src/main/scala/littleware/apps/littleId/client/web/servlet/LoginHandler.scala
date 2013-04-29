/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId.client.web.servlet

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject
import javax.security.auth.login.Configuration
import javax.security.auth.login.LoginContext
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener
import littleware.apps.littleId.client
import client.controller.VerifyTool
import client.web.bean
import littleware.apps.littleId.client.controller.JaasLoginModule
import littleware.base.login.LoginCallbackHandler
import java.util.logging.{Level,Logger}
import littleware.scala.LittleHelper
import littleware.web.beans.GuiceBean
import littleware.web.servlet.WebBootstrap
import scala.collection.JavaConversions._


/**
 * Combination servlet and HttpSessionListener.
 * When a session is created injects a "guest" IdBean with name ${idBean}.
 * If the servlet receives a request, then it looks
 * for:
 *        email, secret, urlSuccess, urlFailure
 * parameters.  The servlet attempts to authenticate
 * a new session, and replaces the Sessin's IdBean as necessary
 */
class LoginHandler extends HttpServlet with HttpSessionListener {
  private val log = Logger.getLogger( getClass.getName )


  private def sessionCreated(session:HttpSession ):Unit = {
    val idBean:bean.IdBean = session.getAttribute( "idBean" ).asInstanceOf[bean.IdBean]
    log.log( Level.FINE, "Session created")
    if (null == idBean) {
      session.setAttribute( "idBean", new bean.IdBean() )
    }
  }

  /**
   * Inject a guest IdBean into the session
   */
  override def sessionCreated( event:HttpSessionEvent ):Unit = {
    sessionCreated( event.getSession() );
  }


  override def sessionDestroyed( event:HttpSessionEvent ):Unit = {}

  var loginOkURL = "/login/view/welcome.jsp"
  var loginFailedURL = "/login/view/ugh.jsp"
  var logoutURL = "/login/view/goodbye.jsp"
  var jaasConfig:Configuration = null

  @Inject
  def injectMe( config:JaasLoginModule.Config ):Unit = {
    this.jaasConfig = config
  }

  /**
   * Allow init-time override of default "loginOkURL", logoutURL,
   * and "loginFailedURL" properties
   */
  override def init():Unit = {
    val config = getServletConfig();
    for( value <- Option( config.getInitParameter("loginOkURL") ) ) {
      loginOkURL = value
    }
    for( value <- Option( config.getInitParameter("loginFailedURL") ) ) {
      loginFailedURL = value
    }
    for( value <- Option( config.getInitParameter("logoutURL") ) ) {
      logoutURL = value
    }
    val gbean:GuiceBean = getServletContext.getAttribute( WebBootstrap.littleGuice ).asInstanceOf[GuiceBean]
    gbean.injectMembers(this)
  }


  @throws( classOf[ServletException])
  override def doPost( request:HttpServletRequest, response:HttpServletResponse ):Unit =
    doCommon(request, response)

  @throws( classOf[ServletException])
  override def doGet( request:HttpServletRequest, response:HttpServletResponse ):Unit =
    doCommon(request, response)

  /**
   * Little utility to redirect the client
   */
  @throws( classOf[ServletException])
  private def redirect( request:HttpServletRequest, response:HttpServletResponse, redirectURL:String ):Unit = try {
    response.sendRedirect( redirectURL )
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Failed redirect to " + redirectURL, ex )
        if ( ex.isInstanceOf[ServletException] ) {
          throw ex
        } else {
          throw new ServletException( "Failed redirect to " + redirectURL, ex )
        }
      }
  }

  @throws( classOf[ServletException])
  private def doCommon( request:HttpServletRequest, response:HttpServletResponse ):Unit = {
    val action = Option( request.getParameter("action") ).map( (param) => { param.trim.toLowerCase } ).getOrElse(
      "login"
    )
    action match {
      case "login" => try {
          val email:String = request.getParameter( "email" )
          val secret:String = request.getParameter( "secret" )
          require( null != email, "Email parameter is required for login")
          require( null != secret, "Secret parameter is required for login")
          log.log( Level.FINE, "Processing login request {0} - {1}", Array[Object]( email, secret ))
          val subject = new Subject()
          (new LoginContext( "littleId", subject,
                            new LoginCallbackHandler( email, secret ),
                            jaasConfig)
          ).login()
          request.getSession.setAttribute( "idBean", new bean.IdBean( subject.getPrincipals.head ) )
          redirect( request, response, LittleHelper.emptyCheck( request.getParameter( "loginOkURL" ) ).getOrElse( loginOkURL ) )
        } catch {
          case  ex:ServletException => throw ex
          case ex:Exception => {
              log.log(Level.WARNING, "Login failed", ex)
              request.setAttribute("exception", ex)
              redirect( request, response,
                Option( request.getParameter("loginFailedURL") ).getOrElse( loginFailedURL )
              )
            }
        }

      case "logout" => {
          request.getSession.setAttribute( "idBean", new bean.IdBean() )
          redirect( request, response, Option( request.getParameter("logoutURL") ).getOrElse( logoutURL ) )
        }
      case _ => {
          log.log( Level.WARNING, "Uknown login action: " + action )
          throw new IllegalArgumentException("Unknown action: " + action);
        }
    }
  }


}
