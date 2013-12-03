/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.base.Options;
import littleware.base.Option;
import littleware.web.beans.GuiceBean;
import littleware.web.servlet.helper.JsonResponse;
import littleware.web.servlet.helper.ResponseHelper;

/**
 * Dispatcher attempts to lookup a LittleServlet in the session scope.
 * Treats URL ending in .../lgo/command as special case - looks up "lgo" bean.
 */
public class LittleDispatcher extends HttpServlet {

    private static final Logger log = Logger.getLogger(LittleDispatcher.class.getName());
    private final Map<String, Class<?>> dispatchMap = new HashMap<String, Class<?>>();

    /**
     * Servlet init accepts mapping from command-name to command-class where
     * command class extends LittleServlet
     */
    @Override
    public void init(ServletConfig config) {
        for (Enumeration<String> scan = config.getInitParameterNames();
                scan.hasMoreElements();) {
            final String key = scan.nextElement();
            final String className = config.getInitParameter(key);
            try {
                final Class<?> value = Class.forName(className);
                dispatchMap.put(key, value);
            } catch (ClassNotFoundException ex) {
                log.log(Level.SEVERE, "Failed to load: " + className, ex);
            }
        }
    }

    private void doGetOrPostOrPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // The command name (url prefix) this URL wants to execute
        final String command;
        {
            final String temp = request.getPathInfo().replaceAll( "/+$", "" );
            if (temp.indexOf("/lgo/") >= 0 ) {
                command = "lgo";
            } else {
                final int lastSlash = temp.indexOf( "/" , 1);
                if (lastSlash >= 0) {
                    command = temp.substring(1, lastSlash );
                } else {
                    command = temp.substring(1);
                }
            }
        }
        // GuiceBean should have been injected by request filter
        final GuiceBean guiceBean = Options.some( (GuiceBean) request.getAttribute( WebBootstrap.littleGuice ) ).get();
        
        // Lookup the LittleServlet registered to handle this command (url)
        final Option<LittleServlet> optHandler;
        {
            Option<LittleServlet> lookup = Options.empty();
            // Allocate service and inject little-session dependencies
            final Class<?> commandClass = dispatchMap.get(command);
            if (null != commandClass) {
                lookup = littleware.base.Options.some( (LittleServlet) guiceBean.getInstance( commandClass ) );
            }
            optHandler = lookup;
        }
        if ( optHandler.isEmpty() ) {
          final JsonResponse.Builder respBuilder = guiceBean.getInstance( JsonResponse.Builder.class );
          final ResponseHelper helper = guiceBean.getInstance( ResponseHelper.class );
          respBuilder.status.set( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
          helper.write(response, respBuilder.build() );
          return;
        }
        optHandler.get().doGetOrPostOrPut(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetOrPostOrPut(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetOrPostOrPut(request, response);
    }
    
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetOrPostOrPut(request, response);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetOrPostOrPut(request, response);
    }

}
