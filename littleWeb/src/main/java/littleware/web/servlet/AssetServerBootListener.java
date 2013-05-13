/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.bootstrap.LittleBootstrap;

/**
 * ServletContextListener manages bootup and shutdown of littleware with an embedded littleAsset server,
 * and injects a default-session GuiceBean into the servlet context attribute map.  
 * When running with an embedded server client-APIs invoke server-APIs via direct function
 * call rather than via RMI or RESTful access.
 * Although littleware server APIs are registered with the Guice injector, 
 * user-level code should restrict itself to injecting littleware client APIs,
 * so that client and server can be easily deployed to separate machines when necessary.
 * See also AppBootstrapListener.
 * 
 * Note: although there is some duplication,
 *   avoid mixing server-setup and generic app-setup code, so that non-asset apps
 *   can avoid pulling in asset dependencies.
 */
public class AssetServerBootListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(AssetServerBootListener.class.getName());
    private Option<LittleBootstrap> boot = Maybe.empty();

  @Override
    public synchronized void contextInitialized(ServletContextEvent sce) {
        try {
            final ServletContext ctx = sce.getServletContext();
            WebBootstrap.bootstrap( ServerBootstrap.provider.get(), ctx );
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed littleware bootstrap", ex);
        }
    }

  @Override
    public synchronized void contextDestroyed(ServletContextEvent sce) {
        if ( boot.isSet() ) {
            log.log(Level.INFO, "Shutting down littleware ...");
            boot.get().shutdown();
            boot = Maybe.empty();
        }
    }
}
