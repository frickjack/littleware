/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.fishRunner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;

/**
 * Download a .war from S3 and deploy to an embedded glassfish server
 */
public class FishApp implements Callable
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
    public static class ConfigException extends Exception {
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
                
                if ( "gzip".equalsIgnoreCase( meta.getContentEncoding() ) ) {
                    // need to unzip the war ...
                    final File temp = new File( destFile.getParentFile(), destFile.getName() + ".gz" );
                    temp.delete();
                    destFile.renameTo( temp );
                    try (
                        final InputStream gzin = new java.util.zip.GZIPInputStream( new FileInputStream( temp ) );
                            ) {
                        Files.copy( gzin, destFile.toPath() );
                    }
                }

                return destFile;
            } catch (URISyntaxException|IOException ex) {
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
    public GlassFish call() throws ConfigException {
        final File warFile = downloadHelper( config.WAR_URI, new File( "deploy.war" ) );
        
        if ( null != config.LOGIN_URI ) { // download and register login.conf
            final File confFile = downloadHelper( config.LOGIN_URI, new File( "deploy.login.conf" ) );
            try {
                System.setProperty( "java.security.auth.login.config", confFile.getCanonicalPath() );
            } catch (IOException ex) {
                throw new ConfigException( "Failed to resolve canonical path for login config: " + confFile, ex );
            }
        }
        try { // deploy war file
            final GlassFish gf = fishFactory.get();
            final Deployer deployer = gf.getDeployer();
            log.log( Level.INFO, "Deploying .war: " + warFile.getCanonicalPath() );
            deployer.deploy( warFile, "--force=true", "--contextroot", config.CONTEXT_ROOT  );
            return gf;
        } catch (GlassFishException|IOException ex) {
            throw new RuntimeException( "Failed war deployment", ex);
        }
    }

    /**
     * Command-line/environment-variable configuration flags
     */
    public static enum Flag {
        S3_KEY, S3_SECRET, S3_CREDSFILE,
        PORT,
        DATABASE_URL, WAR_URI, CONTEXT_ROOT, LOGIN_URI;
    }


    private static final String instructions = 
            "\nfishRunner key value key value ...\nOptions pulled first from system environment,\nthen overriden by command line values:" +
            "\nS3_KEY" +
            "\nS3_SECRET" +
            "\nS3_CREDSFILE  - either both S3_KEY and S3_SECRET or S3_CREDSFILE must be defined" +
            "\nWAR_URI - required - either an s3:// URI otherwise treated as local file path" +
            "\nPORT - optional - defaults to 8080 if not otherwise specified" +
            "\nLOGIN_URI - optional - JAAS login.conf location either and s3:// URI otherwise treated as local file path" +
            "\nCONTEXT_ROOT - required - glassfish deploy context root for war" +
            "\nDATABASE_URL - required - ex: postgres://user:password@host:port/database\n";
    
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

        configMap.put( Flag.PORT.toString(), "8080" );
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

        log.log( Level.INFO, "Setting up runtime environment: " );
        for( String key : configMap.keySet() ) {
            log.log( Level.INFO, key + "='" + configMap.get(key) + "'" );
        }
        
        try {
        
            // sanity check
            for( Flag key : EnumSet.of( Flag.CONTEXT_ROOT, Flag.DATABASE_URL, Flag.WAR_URI ) ) { 
                // LOGIN_URI may be null
                if ( null == configMap.get(key.toString() ) ) {
                    throw new ConfigException( "Parameter must be specified in environment or on command line: " + key );
                }
            }

            final Set<Flag> s3Flags = EnumSet.of( Flag.S3_CREDSFILE, Flag.S3_KEY, Flag.S3_SECRET );
            String s3Key = configMap.get( Flag.S3_KEY.toString() );
            String s3Secret = configMap.get( Flag.S3_SECRET.toString() );
            String s3CredsFile = configMap.get( Flag.S3_CREDSFILE.toString() );

            if ( (null == s3Key) || (null == s3Secret) ) {
                if ( null == s3CredsFile ) {
                    throw new ConfigException( "Must specify (S3_KEY,S3_SECRET) or S3_CREDSFILE" );
                }
                final AWSCredentials creds = new PropertiesCredentials( new java.io.File( s3CredsFile ) );
                s3Key = creds.getAWSAccessKeyId();
                s3Secret = creds.getAWSSecretKey();
            } else if ( null != s3CredsFile ) {
                throw new ConfigException( "Ambiguous S3 credentials - both (S3_KEY,S3_SECRET) and S3_CREDSFILE defined");
            }
        

            // finally - launch the app
            final Config config = new Config( 
                    configMap.get( Flag.WAR_URI.toString() ), 
                    configMap.get( Flag.CONTEXT_ROOT.toString() ), 
                    configMap.get( Flag.LOGIN_URI.toString() )
                    );

            final int port = Integer.parseInt( configMap.get( Flag.PORT.toString()) );
            final Injector ij = Guice.createInjector( 
                    new AppModule( config ),
                    new FishModule( s3Key, s3Secret,
                        new java.net.URI( configMap.get( Flag.DATABASE_URL.toString() ) ),
                        port
                        )
                    );
            
            final FishApp app = ij.getInstance( FishApp.class );
            final GlassFish gf = app.call();
            
            System.out.print( "Enter 'quit' to shutdown server:\n> " );
            System.out.flush();
            final BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
            while( true ) {
                final String input = reader.readLine();
                System.out.print( "\n> " );
                System.out.flush();
                if ( null == input ) {
                    log.log( Level.INFO, "stdin closed - assuming daemon environment - leaving interactive thread");
                    break;
                }
                if ( input.equals( "quit" ) ) {
                    log.log( Level.INFO, "Shutting down ..." );
                    gf.stop();
                    Thread.sleep( 5000 );
                    System.exit(0);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, 
                    "Failed to launch webapp",
                    ex);
            log.log( Level.INFO, instructions );
            System.exit(1);
        }
    }
}
