/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.bootstrap.internal;

import com.google.inject.Binder;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.asset.server.web.servlet.AssetSearchServlet;
import littleware.bootstrap.AppBootstrap;
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
 * Setup embedded jetty server - this still needs work ....
 */
public class JettyServerModule extends AbstractServerModule {

    private static final Logger log = Logger.getLogger(AssetServerModule.class.getName());

    private JettyServerModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    @Override
    public void configure(Binder binder) {
    }

    public static class Activator implements BundleActivator {

        private final Server server;
        private final AssetSearchServlet searchServlet;

        @Inject
        public Activator(
                Server server,
                AssetSearchServlet searchServlet) {
            this.server = server;
            this.searchServlet = searchServlet;
        }

        @Override
        public void start(BundleContext bc) throws Exception {
            try {

                // Startup Jetty
                final Connector connector = new BlockingChannelConnector();
                connector.setPort(1238);
                connector.setHost("127.0.0.1");
                server.addConnector(connector);

                final ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
                servletHandler.setContextPath("/littleware");
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
            log.log(Level.INFO, "littleware shutdown ok");
        }
    }

    @Override
    public Class<Activator> getActivator() {
        return Activator.class;
    }

    public static class Factory implements ServerModuleFactory {

        @Override
        public ServerModule buildServerModule(AppBootstrap.AppProfile profile) {
            return new JettyServerModule(profile);
        }
    }
}
