/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId.client.web.servlet


import java.io.IOException
import java.util.logging.Level
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import littleware.apps.littleId.client.web.bean.IdBean
import java.util.logging.{Level,Logger}


/**
 * Little filter that verifies that a user is logged in before
 * accessing paths matching an init-configured pattern.
 */
class SecurityFilter extends Filter {
  private val log = Logger.getLogger( getClass.getName )

  /**
   * Context-relative path to login form
   */
  var loginPath = "/home.jsf";

  @throws( classOf[ServletException] )
  override def init( config:FilterConfig ):Unit = {
    Option( config.getInitParameter("loginForm") ).foreach( (uri) => {
        if (uri.startsWith("/")) {
          loginPath = uri
        } else {
          loginPath = "/" + uri;
        }
      }
    )
    log.log(Level.INFO, "Login URI set to : " + loginPath);
  }

  /** Do nothing */
  override def destroy():Unit = {
  }


  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  private def redirectToLogin( sreq:ServletRequest,  sres:ServletResponse ):Unit =  {
    val request = sreq.asInstanceOf[HttpServletRequest]
    val response = sres.asInstanceOf[HttpServletResponse]
    /*...
    val uri = request.getRequestURI()
    val context = request.getContextPath()
    val contextRelativeUri = (if ((!context.isEmpty()) && uri.startsWith(context)) {
        uri.substring(context.length())
      } else {
        uri
      })
    val redirect = {
      val prefix = context + loginPath
      if (prefix.indexOf('?') > 0) {
        prefix + "&loginTrigger=" + contextRelativeUri;
      } else {
        prefix + "?loginTrigger=" + contextRelativeUri;
      }
    }
    ...*/
    request.getRequestDispatcher( loginPath ).forward( request, response )
    //response.sendRedirect(redirect);
  }

  /**
   * Lookup the littleware.web.beans.SessionBean from the &quot;lw_user&quot;
   * session attribute, and extract the Subject to set
   * the javax.security.auth.subject session attribute to.
   */
  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override def doFilter( sreq:ServletRequest, sres:ServletResponse,
                        chain:FilterChain ):Unit = {
    val request = sreq.asInstanceOf[HttpServletRequest]
    val response = sres.asInstanceOf[HttpServletResponse]
    val session = request.getSession(true)
    val isLoggedIn = Option( session.getAttribute( "idBean" ) ).map( (bean) => { ! bean.asInstanceOf[IdBean].isGuest } ).getOrElse(false)
    if ( ! isLoggedIn ) {
      redirectToLogin(sreq, sres);
    } else {
      chain.doFilter(sreq, sres)
    }
  }
}
