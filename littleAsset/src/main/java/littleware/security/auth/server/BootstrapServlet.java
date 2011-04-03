/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.*;
import javax.servlet.*;
import littleware.asset.server.bootstrap.ServerBootstrap;

/**
 * Servlet bootstraps littleware server components
 * within a J2EE container.
 *
 * TODO: configure GUICE injector bean initialization
 */
public class BootstrapServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger( BootstrapServlet.class.getName() );
    private static final long serialVersionUID = 513807516080701142L;

    private Exception        lastError = null;
    private littleware.asset.server.bootstrap.ServerBootstrap  bootstrap = null;
    
    /**
     * Start up the littleware backend
     * 
     * @param config parameters from web.xml and the appserver
     * @throws javax.servlet.ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);

        try {
            bootstrap = ServerBootstrap.provider.get().profile(ServerBootstrap.ServerProfile.J2EE).build();
            //boot.getOSGiActivator().add( PackageTestSuite.class );
            bootstrap.bootstrap();
            log.log( Level.INFO, "Littleware Bootstrap ok" );
            //SessionUtil.get().getSessionManager ();
        } catch ( Exception ex ) {
            lastError = ex;
            log.log( Level.SEVERE, "Failed to bootstrap Littleware SessionManager", ex );
            throw new ServletException( "Failed to bootstrap", ex );
        }
    }

    @Override
    public void destroy () {
        log.log( Level.INFO, "Attempting to shutdown littleware OSGi runtime" );
        bootstrap.shutdown();
    }
        
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            	throws ServletException, IOException {
        PrintWriter writer = res.getWriter ();
        
        writer.print( "<html>\n<head><title>Bootstrap Status</title></head>\n<body>");
        writer.print( "<h3>Bootstrap Status</h3>\n" );
        if ( lastError == null ) {
            writer.append( "<p> Bootstrap ok.</p>\n" );
        } else {
            writer.append( "<p> Bootstrap failed, caught: " + lastError.getMessage () +
                    ", see logs for details.</p>\n"
                    );
        }
        writer.append( "</body></html>\n" );
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
        doPost( req,res );
    }
}
