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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.bootstrap.AppBootstrap;

/**
 * ServletContextListener manages bootup and shutdown of littleware in "application"
 * mode (no embedded littleAsset server), and injects a default-session 
 * GuiceBean into the servlet context attribute map.  See also AssetServerBootListener.
 */
public class AppBootstrapListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(AppBootstrapListener.class.getName());
    private Option<AppBootstrap> boot = Maybe.empty();

    public synchronized void contextInitialized(ServletContextEvent sce) {
        try {
            boot = Maybe.something(WebBootstrap.bootstrap(sce.getServletContext()) );
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed littleware bootstrap", ex);
        }
    }

    public synchronized void contextDestroyed(ServletContextEvent sce) {
        if ( boot.isSet() ) {
            log.log(Level.INFO, "Shutting down littleware ...");
            boot.get().shutdown();
            boot = Maybe.empty();
        }
    }
}
