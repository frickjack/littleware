/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.security.auth.SessionManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Startup the RMI-registyr if necessary, and registry
 * the remote SessionManager
 */
public class ServerActivator implements BundleActivator {
    private static final Logger  olog = Logger.getLogger( ServerActivator.class.getName() );

    private final SessionManager   omgr_session;
    private final int              oi_registry_port;
    
    @Inject
    public ServerActivator( SessionManager mgr_session, @Named( "int.lw.rmi_port" ) int i_registry_port ) {
        omgr_session = mgr_session;
        oi_registry_port = i_registry_port;
    }

    /**
     * Startup RMI registry if necessary and bind SessionManager
     *
     * @param ctx
     * @throws java.lang.Exception
     */
    public void start(BundleContext ctx) throws Exception {
        try {
            Registry rmi_registry = null;
            final int i_port = oi_registry_port;

            try {
                olog.log(Level.INFO, "Looking for RMI registry on port: " + i_port);
                rmi_registry = LocateRegistry.createRegistry(i_port);
            } catch ( Exception e ) {
               olog.log( Level.SEVERE, "Failed to locate or start RMI registry on port " + i_port +
                            " running without exporting root SessionManager object to RMI universe", e
                            );

                rmi_registry = LocateRegistry.getRegistry( i_port );
            }

            /**
             * Need to wrap session manager with an invocation handler,
             * because the RMI server thread inherits the ActivationContext
             * of the client thread.  Frick.
             */
            /**
             * Publish the reference in the Naming Service using JNDI API
             * Context jndi_context = new InitialContext();
             * jndi_context.rebind("/littleware/SessionManager", om_session );
             */
            rmi_registry.rebind("littleware/SessionManager", omgr_session );
        } catch (Exception e) {
            //throw new AssertionFailedException("Failed to setup SessionManager, caught: " + e, e);
            olog.log( Level.SEVERE, "Failed to bind to RMI registry " +
                                " running without exporting root SessionManager object to RMI universe",
                                e
                                );

        }
        olog.log( Level.INFO, "littleware RMI start ok" );
    }

    public void stop(BundleContext ctx) throws Exception {
        olog.log( Level.INFO, "littleware shutdown ok" );
    }

}
