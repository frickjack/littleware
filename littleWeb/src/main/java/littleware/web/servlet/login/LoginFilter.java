/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.web.beans.GuiceBean;
import littleware.web.servlet.WebBootstrap;

/**
 * Works in conjunction with LoginServlet to setup 
 * authenticated littleware environment that servlets
 * or whatever can access via session.getAttribute( "guiceBean" )
 */
public class LoginFilter implements Filter {
  public static final String guiceBean = WebBootstrap.littleGuice;
  public static final String littleCookie = LoginServlet.littleCookieName;
  private static final Logger log = Logger.getLogger( LoginFilter.class.getName() );

  Option<GuiceBean> optGuice = Maybe.empty();
  private final Gson gsonTool = new Gson();
  
  @Override
  public void init(FilterConfig fc) throws ServletException {
    // noop
    optGuice = Maybe.something((GuiceBean) fc.getServletContext().getAttribute(WebBootstrap.littleGuice));
  }

  /**
   * Just make sure the freakin' littleware session is initialized ...
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    
    if ( optGuice.nonEmpty() && null == req.getAttribute( guiceBean ) ) {
      try {
        
        final Option<JsonObject> optSessionInfo;
        {
          JsonObject js = null;
          final Cookie[] cookies = req.getCookies();
          for (Cookie cookie : cookies) {
            if (cookie.getName().equals(littleCookie)) {
              js = gsonTool.fromJson( cookie.getValue(), JsonElement.class ).getAsJsonObject();
            }
          } 
          optSessionInfo = Maybe.something( js );
        }

        // TODO: setup LoadingCache for session bla bla
        if ( optSessionInfo.nonEmpty() ) {
          final UUID sessionId = UUIDFactory.parseUUID( optSessionInfo.get().get( "id" ).getAsString() );
        } 

      } catch (IllegalStateException ex) {
        log.log(Level.INFO, "Ignoring webapp state exception setting up session - some weird glassfish race condition", ex);
      }
    } else {
      log.log(Level.WARNING, "Application level GUICE injection not configured");
    }
    chain.doFilter(request, response);
  }

  public void destroy() {}
}
