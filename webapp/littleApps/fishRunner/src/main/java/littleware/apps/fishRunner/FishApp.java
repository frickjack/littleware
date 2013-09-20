/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.fishRunner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;

/**
 * Download a .war from S3 and deploy to an embedded glassfish server
 */
public class FishApp implements Runnable
{
    private static final Logger log = Logger.getLogger( FishApp.class.getName() );
    
    private final Provider<GlassFish> fishFactory;
    private final AmazonS3 s3;
    private final Config config;
    
    /**
     * Little POJO for FishApp configuration props
     */
    public static final class Config {
        public final String WAR_URI;
        public final String CONTEXT_ROOT;
        public final String LOGIN_URI;
        
        /**
         * @param WAR_URI s3:// uri to download WAR file to, otherwise treated as local file path
         * @param CONTEXT_ROOT glassfish domain root under which to deploy downloaded war
         * @param LOGIN_URI s3:// uri of login.conf file to download and register, otherwise treated as local file path - or null if not needed
         */
        public Config( String WAR_URI, String CONTEXT_ROOT, String LOGIN_URI ) {
            this.WAR_URI = WAR_URI;
            this.CONTEXT_ROOT = CONTEXT_ROOT;
            this.LOGIN_URI = LOGIN_URI;
        }
    }

    /**
     * Exception on failure to parse user-supplied configuration or
     * retrieve user-specified resource - that kind of thing.
     */
    public static class ConfigException extends RuntimeException {
        public ConfigException( String message ) { super( message ); }
        public ConfigException( String message, Throwable cause ) { super( message, cause ); }
    }
    
    /**
     * Module just binds Config into the guice object graph
     */
    public static final class AppModule implements Module {
        private final Config config;
        
        public AppModule( Config config ) {
            this.config = config;
        }
        
        @Override
        public void configure(Binder binder) {
            binder.bind( Config.class ).toInstance( config );
        }        
    }
    
    @Inject
    public FishApp( Provider<GlassFish> fishFactory, AmazonS3 s3, Config config ) {
        this.fishFactory = fishFactory;
        this.s3 = s3;
        this.config = config;
    }

    /**
     * Download the given S3 URI to the given dest file
     * 
     * @param resourcePath path string either starts with s3:// or treated as local path
     * @param destFile
     * @return local file to work with if resource exists, otherwise throws ConfigException
     */
    private File downloadHelper( String resourcePath, File destFile ) throws ConfigException {
        if ( resourcePath.startsWith( "s3://" ) ) {
            try {
                final URI s3URI = new java.net.URI( resourcePath );
                final GetObjectRequest req = new GetObjectRequest( 
                        s3URI.getHost(),
                        s3URI.getPath().replaceAll( "//+", "/" ).replaceAll( "^/+", "" ).replaceAll( "/+$", "" )
                        );

                final ObjectMetadata meta = s3.getObject( req, destFile );
                if ( null == meta ) {
                    throw new RuntimeException( "Unable to access resource: " + resourcePath );
                }
                return destFile;
            } catch (URISyntaxException ex) {
                throw new RuntimeException( "Failed parsing: " + resourcePath, ex );
            }
        } else {
            final File resourceFile = new File( resourcePath );
            if ( ! resourceFile.exists() ) {
                throw new RuntimeException( "Unable to access resource: " + resourcePath );
            }
            return resourceFile;
        }
    }
    

    /**
     * Download and deploy the war specified in this.config
     */
    @Override
    public void run() {
        final File warFile = downloadHelper( config.WAR_URI, new File( "deploy.war" ) );
        
        if ( null != config.LOGIN_URI ) { // download and register login.conf
            final File confFile = downloadHelper( config.LOGIN_URI, new File( "deploy.login.conf" ) );
            try {
                System.setProperty( "java.security.auth.login.config", confFile.getCanonicalPath() );
            } catch (IOException ex) {
                throw new RuntimeException( "Failed to resolve canonical path for login config: " + confFile, ex );
            }
        }
        try { // deploy war file
            final GlassFish gf = fishFactory.get();
            final Deployer deployer = gf.getDeployer();
            deployer.deploy( warFile, "--force=true", "--contextroot", config.CONTEXT_ROOT  );
        } catch (GlassFishException ex) {
            throw new RuntimeException( "Failed war deployment", ex);
        }
    }

    /**
     * Command-line/environment-variable configuration flags
     */
    public static enum Flag {
        S3_KEY, S3_SECRET,
        DATABASE_URL, WAR_URI, CONTEXT_ROOT, LOGIN_URI;
    }

    /**
     * Pulls in configuration from command line or falls back to environment variables
     * of the same name. 
     * Command line flags: S3_KEY, S3_SECRET, DATABASE_URL, WAR_URI, CONTEXT_ROOT.
     * DATABASE_URL must be of form postgres://user:password@host:port/database -
     * we currently only support postgres database.
     * WAR_URI must be of form s3://... - we currently only support s3: URL's.
     * The app downloads the .war from WAR_URI using AWS credentials S3_KEY and S3_SECRET,
     * launches glassfish on port 8080, establishes a database resource
     * at jndi://jdbc/littleDB connected to DATABASE_URL,
     * and deploys the downloaded WAR to CONTEXT_ROOT if set - 
     * otherwise assumes the .war includes a 
     * glassfish_web.xml that specifies the context root.
     * 
     * @param args command line args
     */
    public static void main( String[] args )
    {
        final Map<String,String> configMap = new HashMap<>();
        
        for( Flag key : Flag.values() ) {  // scan environment
            configMap.put( key.toString(), System.getenv(key.toString()));
        }

        { // command line overrides
            String key = null;
            for( String value : args ) {
                if ( null == key ) {
                    key = value;
                } else {
                    configMap.put( key, value );
                    key = null;
                }
            }
        }
        
        for( Flag key : Flag.values() ) { // sanity check
            // LOGIN_URI may be null
            if ( (! key.equals( Flag.LOGIN_URI )) && (null == configMap.get(key.toString())) ) {
                throw new RuntimeException( "Parameter must be specified in environment or on command line: " + key );
            }
        }
        
        try {
            // finally - launch the app
            final Config config = new Config( 
                    configMap.get( Flag.WAR_URI.toString() ), 
                    configMap.get( Flag.CONTEXT_ROOT.toString() ), 
                    configMap.get( Flag.LOGIN_URI.toString() )
                    );

            final Injector ij = Guice.createInjector( 
                    new AppModule( config ),
                    new FishModule( configMap.get( Flag.S3_KEY.toString() ), 
                        configMap.get( Flag.S3_SECRET.toString() ), 
                        new java.net.URI( configMap.get( Flag.DATABASE_URL.toString() ) )
                        )
                    );
            
            final FishApp app = ij.getInstance( FishApp.class );
            app.run();
            
        } catch (URISyntaxException ex) {
            log.log(Level.SEVERE, 
                    "Failed to parse some URI - DATABASE_URL: " + configMap.get( Flag.DATABASE_URL.toString() ),
                    ex);
            System.exit(1);
        }
    }
}
