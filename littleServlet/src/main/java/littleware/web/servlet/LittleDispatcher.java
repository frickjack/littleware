/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
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
import javax.servlet.http.HttpSession;
import littleware.web.beans.GuiceBean;

/**
 * Dispatcher attempts to lookup a LittleServlet in the session scope.
 * Treats URL ending in .../lgo/command as special case - looks up "lgo" bean.
 */
public class LittleDispatcher extends HttpServlet {

    private static final Logger log = Logger.getLogger(LittleDispatcher.class.getName());
    private final Map<String, Class<?>> dispatchMap = new HashMap<String, Class<?>>();

    /**
     * Servlet init accepts mapping from command-name to command-class where
     * command class extends LittleDispatcher
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

    private void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String command;
        {
            final String temp = request.getPathInfo();
            if (temp.indexOf("/lgo/") >= 0 ) {
                command = "lgo";
            } else {
                final int lastSlash = temp.lastIndexOf('/');
                if (lastSlash >= 0) {
                    command = temp.substring(lastSlash + 1);
                } else {
                    command = temp;
                }
            }
        }
        final HttpSession session = request.getSession();
        String message = "Command not registered: " + command;
        Object lookup = session.getAttribute(command);
        if (null == lookup) {
            // Allocate service and inject little-session dependencies
            final Class<?> commandClass = dispatchMap.get(command);
            if (null != commandClass) {
                try {
                    // GuiceBean should have been injected by LoginHandler
                    final GuiceBean guiceBean = (GuiceBean) session.getAttribute(WebBootstrap.littleGuice);
                    if (null == guiceBean) {
                        message = "No guiceBean registered in session: " + WebBootstrap.littleGuice;
                    } else {
                        final LittleServlet service = (LittleServlet) commandClass.newInstance();
                        service.setGuiceBean(guiceBean);
                        session.setAttribute(command, service);
                        lookup = service;
                    }
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Failed to allocate command class", ex);
                    message = "Failed to alocate " + commandClass.getName();
                }
            }
        }
        if (null == lookup) {
            final PrintWriter writer = response.getWriter();
            writer.println("<html><head><title>command not available</title></head><body><p>Command not available: ");
            writer.println( command + ", " + message );
            writer.println("</p></body></html>");
            return;
        }
        if (!(lookup instanceof LittleServlet)) {
            final PrintWriter writer = response.getWriter();
            writer.println("<html><head><title>command of wrong type</title></head><body><p>Command not a LittleServlet: ");
            writer.println(command);
            writer.println("</p></body></html>");
            return;
        }
        ((LittleServlet) lookup).doGetOrPost(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetOrPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetOrPost(request, response);
    }
}