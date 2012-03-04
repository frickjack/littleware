/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.webproxy;

import com.google.inject.Binder;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Optional module starts Jetty server, binds Server and /littleware/ ServletContext,
 * and registers asset.webproxy servlets
 */
public class JettyModule implements littleware.bootstrap.AppModule {

    private final AppProfile profile;

    public JettyModule(AppProfile profile) {
        this.profile = profile;
    }

    @Override
    public AppProfile getProfile() {
        return profile;
    }

    @Override
    public Class<? extends BundleActivator> getActivator() {
        return JettyActivator.class;
    }

    @Override
    public void configure(Binder binder) {
        final Server server = new Server();
        final ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/littleware");
        binder.bind( Server.class ).toInstance( server );
        binder.bind( ServletContextHandler.class ).toInstance( servletHandler );
    }
    
    public static class JettyActivator implements BundleActivator {
        private static final Logger log = Logger.getLogger( JettyActivator.class.getName() );
        private final Server server;
        private final ServletContextHandler servletHandler;
        private final AssetSearchServlet searchServlet;

        @Inject
        public JettyActivator(
                Server server,
                ServletContextHandler servletHandler,
                AssetSearchServlet searchServlet) {
            this.server = server;
            this.servletHandler = servletHandler;
            this.searchServlet = searchServlet;
        }

        @Override
        public void start(BundleContext bc) throws Exception {
            try {
                // Startup Jetty
                final Connector connector = new BlockingChannelConnector();
                connector.setPort(1238);
                connector.setHost("127.0.0.1");
                //connector.setHost( "localhost" );
                server.addConnector(connector);
                //server.setHandler(context);
                servletHandler.addServlet(new ServletHolder(searchServlet), "/services/search/*");

                final ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setDirectoriesListed(true);
                resourceHandler.setWelcomeFiles(new String[]{"index.html"});
                resourceHandler.setResourceBase(".");

                final HandlerList handlers = new HandlerList();
                handlers.setHandlers(new Handler[]{resourceHandler, servletHandler, new DefaultHandler()});
                server.setHandler(handlers);

                //context.addServlet(new ServletHolder(thumbServlet),"/thumb/*");
                server.start();

            } catch (Exception ex) {
                //throw new AssertionFailedException("Failed to setup SessionManager, caught: " + e, e);
                log.log(Level.SEVERE, "Failed to bind to RMI registry "
                        + " running without exporting root SessionManager object to RMI universe",
                        ex);

            }
            log.log(Level.INFO, "littleware RMI and REST start ok");
        }

        @Override
        public void stop(BundleContext bc) throws Exception {
            server.stop();
            log.log(Level.INFO, "Jetty shutdown ok");
        }
    }
    
    public static class AppFactory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new JettyModule(profile);
        }
    }
    
}
