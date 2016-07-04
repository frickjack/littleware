package littleware.asset.webproxy;

import com.google.inject.Binder;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.Option;
import littleware.base.Options;
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
    public Option<Class<JettyActivator>> getCallback() {
        return Options.some( JettyActivator.class );
    }

    @Override
    public void configure(Binder binder) {
        final Server server = new Server();
        final ServletContextHandler servletHandler = new ServletContextHandler( ServletContextHandler.SESSIONS );
        servletHandler.setContextPath("/littleware");
        
        // default resource handler configuration
        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(".");
        
        binder.bind( Server.class ).toInstance( server );
        binder.bind( ServletContextHandler.class ).toInstance( servletHandler );
        binder.bind( ResourceHandler.class ).toInstance( resourceHandler );
    }
    
    public static class JettyActivator implements LifecycleCallback {
        private static final Logger log = Logger.getLogger( JettyActivator.class.getName() );
        private final Server server;
        private final ServletContextHandler servletHandler;
        // private final AssetSearchServlet searchServlet;
        private final ResourceHandler resourceHandler;

        @Inject
        public JettyActivator(
                Server server,
                ServletContextHandler servletHandler,
                ResourceHandler resourceHandler
                //AssetSearchServlet searchServlet
                ) {
            this.server = server;
            this.servletHandler = servletHandler;
            // this.searchServlet = searchServlet;
            this.resourceHandler = resourceHandler;
        }

        @Override
        public void startUp(){
            try {
                // Startup Jetty
                final Connector connector = new BlockingChannelConnector();
                connector.setPort(1238);
                connector.setHost("127.0.0.1");
                //connector.setHost( "localhost" );
                server.addConnector(connector);
                //server.setHandler(context);
                // servletHandler.addServlet(new ServletHolder(searchServlet), "/services/search/*");

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
        public void shutDown(){
            try {
                server.stop();
                log.log(Level.INFO, "Jetty shutdown ok");
            } catch (Exception ex) {
                log.log(Level.WARNING, "Jetty shutdown error", ex);
            }
        }
    }
    
    public static class AppFactory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new JettyModule(profile);
        }
    }
    
}
