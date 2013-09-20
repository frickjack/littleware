/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.fishRunner.test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.apps.fishRunner.FishModule;

/**
 * Setup and run through all the tests ...
 */
public class PackageTest extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTest.class.getName() );
    
    @Inject
    public PackageTest( Provider<FishCase> fishTestFactory ) {
        super( PackageTest.class.getName() );
        this.addTest( fishTestFactory.get() );
        
        this.addTest( new TestCase() {
            @Override public void runTest() {
                try {
                    while( true ) {
                        log.log( Level.INFO, "Looping to prevent test exit - try to connect to test URL now ...");
                        Thread.sleep( 25000 );
                    }
                } catch ( Exception ex ) {}
            }
        } );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        try {
            // TODO - load config variables from properties file ... or do it in FishModule ?
            final String propsPath = PackageTest.class.getName().replaceAll( "\\.+", "/" ) + ".properties";
            final InputStream propStream = PackageTest.class.getClassLoader().getResourceAsStream( propsPath );
            final Properties  props = new Properties();
            if ( null != propStream ) {
                try {
                    props.load(propStream);
                } finally { propStream.close(); }
            }
            Assert.assertTrue( "Required properties found in resource: " + propsPath, 
                    props.containsKey( "test.war" ) &&
                    props.containsKey( "test.s3creds" ) &&
                    props.containsKey( "test.dbURI" )
                    );
            final AWSCredentials creds = new PropertiesCredentials( new java.io.File( props.getProperty("test.s3creds")) );
            final Injector ij = Guice.createInjector( 
                    new FishModule( creds.getAWSAccessKeyId(), creds.getAWSSecretKey(), 
                        new java.net.URI( props.getProperty( "test.dbURI" )) 
                    ),
                    new Module() {
                        @Override
                        public void configure(Binder binder) {
                            binder.bind( FishCase.TestConfig.class 
                                    ).toInstance( new FishCase.TestConfig( new File( props.getProperty( "test.war" )) ) 
                                    );
                        }
                    }
                   );
            
            return ij.getInstance( PackageTest.class );
        } catch (Exception ex) {
            throw new RuntimeException( "Failed test setup", ex );
        }
    }

}
