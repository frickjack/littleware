/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.db;

import com.google.inject.Binder;
import com.google.inject.Module;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import littleware.base.AssertionFailedException;
import littleware.base.PropertiesGuice;
import littleware.base.PropertiesLoader;

/**
 * Setup DataSource bindings in the GUICE module.
 * Logs and eats exceptions setting up DataSource bindings.
 * Just extends PropertiesGuice( littleware_jdbc.properties )
 */
public class DbGuice implements Module {
    private static final Logger olog = Logger.getLogger( DbGuice.class.getName() );


    @Override
    public void configure(Binder binder) {
        try {
            Properties props = PropertiesLoader.get().loadProperties( "littleware_jdbc.properties" );
            new PropertiesGuice( props ).configure( binder );
        } catch ( IOException ex ) {
            throw new AssertionFailedException( "Unable to load littleware_jdbc.properties file", ex );
        }
    }

}
