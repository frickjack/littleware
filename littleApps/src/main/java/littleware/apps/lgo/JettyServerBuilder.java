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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Builder for Jetty-based lgo command server
 */
@Singleton
public class JettyServerBuilder implements LgoServer.ServerBuilder {
    private static final Logger log = Logger.getLogger( JettyServerBuilder.class.getName() );
    private final LgoServlet servlet;

    public static final int serverPort = 19898;
    
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
    public JettyServerBuilder( LgoServlet servlet ) {
        this.servlet = servlet;
    }

    @Override
    public LgoServer launch() {
        final Server server = new Server();
        final Connector connector = new BlockingChannelConnector();
        connector.setPort( serverPort );
        connector.setHost( "127.0.0.1" );
        server.addConnector(connector);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/n9n");
        server.setHandler(context);

        context.addServlet(new ServletHolder(servlet),"/lgo/*");
        try {
            server.start();
            //server.join();
        } catch (Exception ex) {
            throw new IllegalStateException( "Failed server launch", ex );
        }

        return new JettyServer( server );
    }
}
