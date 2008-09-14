/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;

/**
 * Guice module for bootstrapping the LittleGo 
 * application.  Sets up easy Lgo implementation.
 */
public class EzModule implements Module {

    
    /**
     * If we decide to extend littlego into a shell
     * or BSF/scripting environment, then <br />
     * TODO:
     *      <ul>
     *      <li> Move command mapping to a properties file</li>
     *      <li> Setup XML multilingual help system </li>
     *      </ul>
     * @param binder_in
     */
    public void configure( Binder binder_in ) {
        final       LgoCommandDictionary  parser = new EzLgoCommandDictionary ();
        final       LgoHelpLoader         helper = new XmlLgoHelpLoader ();
                
        // Use provider - problem with class loader in Tomcat environment
        binder_in.bind( LgoCommandDictionary.class ).to( EzLgoCommandDictionary.class )
                .in( Scopes.SINGLETON );
        binder_in.bind( LgoHelpLoader.class ).to( XmlLgoHelpLoader.class )
                .in( Scopes.SINGLETON );
        
                         
    }
}