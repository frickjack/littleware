/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Builder for Jetty-based lgo command server
 */
@Singleton
public class JettyServerBuilder implements LgoServer.ServerBuilder {
    private static final Logger log = Logger.getLogger( JettyServerBuilder.class.getName() );

    
    private class HelloServlet extends HttpServlet {
        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello World</h1>");
            response.getWriter().println("session=" + request.getSession(true).getId());
        }
    }

    private static class JettyServer implements LgoServer {
        private final Server server;
        public JettyServer( Server server ) {
            this.server = server;
        }

        @Override
        public boolean isShutdown() {
            return server.isStopped();
        }

        @Override
        public void shutdown() {
            if ( isShutdown() ) {
                return;
            }
            try {
                server.stop();
            } catch (Exception ex) {
                log.log(Level.WARNING, "LGO server shutdown failed", ex);
            }
        }
    }

    @Inject
    public JettyServerBuilder() {
    }

    @Override
    public LgoServer launch() {
        final Server server = new Server(9898);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new HelloServlet()),"/*");
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            throw new IllegalStateException( "Failed server launch", ex );
        }

        return new JettyServer( server );
    }
}
