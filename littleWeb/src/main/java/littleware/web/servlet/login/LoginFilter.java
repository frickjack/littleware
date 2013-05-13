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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
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
  private final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();

          
          
  @Override
  public void init(FilterConfig fc) throws ServletException {
    // noop
    optGuice = Maybe.something((GuiceBean) fc.getServletContext().getAttribute(WebBootstrap.littleGuice));
  }

  /**
   * Just make sure the freakin' littleware session is initialized ...
   */
  @Override
  public void doFilter(ServletRequest requestIn, ServletResponse responseIn, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) requestIn;
    final HttpServletResponse resp = (HttpServletResponse) responseIn;
    
    if ( optGuice.nonEmpty() && 
            (null == req.getSession(false) 
            || null == req.getSession().getAttribute( guiceBean ))
            ) {
        final HttpSession hsession = req.getSession();
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

        if ( optSessionInfo.nonEmpty() ) {
          try {
            final JsonObject js = optSessionInfo.get();
            final UUID sessionId = UUIDFactory.parseUUID( js.get( "id" ).getAsString() );
            final SessionMgr sessionMgr = optGuice.get().getInjector().get().getInstance( SessionMgr.class );
            final SessionInfo sinfo = SessionInfo.fromJson(sessionMgr, js);
            hsession.setAttribute( WebBootstrap.littleGuice, sinfo.getGBean() );
            chain.doFilter(req, resp);
          } catch ( Exception ex ) {
            log.log(Level.INFO, "Failed to establish session from cookie: " + optSessionInfo.get(), ex);
            final JsonObject jsResult = new JsonObject();
            jsResult.addProperty( "status", "error" );
            jsResult.addProperty( "statusInfo", "failed to establish session - probably need to re-login" );
            resp.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            resp.setContentType( mimeMap.getContentType( "bla.js" ) );
            final Writer writer = new OutputStreamWriter( resp.getOutputStream(), Whatever.UTF8 );
            try {
              writer.write( gsonTool.toJson( jsResult ) );
            } finally { writer.flush(); }
          }          
        } else {
          chain.doFilter(req, resp);
        }
    } else {
      log.log(Level.WARNING, "Application level GUICE injection not configured");
      chain.doFilter(req, resp);
    }
    
  }

  public void destroy() {}
}
