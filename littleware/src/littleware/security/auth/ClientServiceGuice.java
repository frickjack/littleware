/*
 * Copyright (c) 2008 Reuben Pasquini
 * All Rights Reserved
 */

package littleware.security.auth;

import java.rmi.Remote;
import java.util.logging.Logger;

import com.google.inject.Binder;
import com.google.inject.Provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import littleware.asset.AssetRetriever;
import littleware.base.AssertionFailedException;
import littleware.base.PropertiesLoader;
import littleware.base.UUIDFactory;
import littleware.base.swing.JPasswordDialog;


/**
 * Bind the implementation of each 
 *    ServiceType.getMembers()
 * interface class
 * to SessionHelper.getService( n_type );
 * This module must be initialized with the SessionHelper
 * from which to acquire service implementations.
 * Also binds LittleSession to helper.getSession
 */
public class ClientServiceGuice implements LittleGuiceModule {
    private static final Logger    olog = Logger.getLogger( ClientServiceGuice.class.getName() );
    private SessionHelper    ohelper = null;

    /**
     * Inject helper dependency
     * 
     * @param helper
     */
    public ClientServiceGuice( SessionHelper helper ) {
        ohelper = helper;
    }
    
    /**
     * Parameterless constructor - client must inject
     * dependency by hand before configuring a GUICE injector,
     * otherwise Guice configure will first attempt to setup
     * a session with data from the last_session.properties file,
     * and finally will prompt the user for name/password
     * using the registered CallbackHandler.
     */
    public ClientServiceGuice() {}
    
    private CallbackHandler  ohandler = new JPasswordDialog ( "", "" );
    
    /**
     * Allow the user to inject a CallbackHandler, otherwise
     * defaults to littleware.base.JPasswordDialog
     */
    public ClientServiceGuice( CallbackHandler handler ) {
        ohandler = handler;
    }
    
    public SessionHelper getSessionHelper () { return ohelper; }
    public void setSessionHelper ( SessionHelper helper ) {
        ohelper = helper;
    }

    
    private static <T extends Remote> Provider<T> bind( final Binder binder,
            final ServiceType<T> service, final SessionHelper helper ) 
    {
        Provider<T> provider =   new Provider<T> () {
                    public T get () {
                        try {
                            return helper.getService( service );
                        } catch ( Exception e ) {
                            throw new littleware.base.FactoryException( "service retrieval failure", e );
                        }
                    }
        };
        binder.bind( service.getServiceInterface() ).toProvider( provider );
        return provider;
    }
    
    private static final String  os_propfile = "latest_session.properties";
    private static final String  os_name_key = "session.username";
    private static final String  os_password_key = "session.password";
    private static final String  os_session_key = "session.id";
    
    /**
     * Retrieve a SessionHelper for the current environment using the
     * given CallbackHandler to prompt the user if necessary,
     * but first attempt to derive session info from latest_session.properties.
     * 
     * @param manager to authenticate against
     * @param handler to prompt user username/password
     * @param i_retry number of retries to allow the user
     * @return authenticated SessionHelper 
     * @throws javax.security.auth.login.LoginException on credential failure
     * @throws java.io.IOException if user cancels out of login prompt
     */
    public static SessionHelper authenticate ( SessionManager manager, 
            CallbackHandler handler,
            int i_retry
            ) throws IOException, LoginException {
        
        if ( i_retry < 1 ) {
            i_retry = 1;
        }
        Properties  prop_session = new Properties ();
        try {
            prop_session = PropertiesLoader.get ().loadProperties( os_propfile );
        } catch ( IOException ex ) {
            olog.log( Level.INFO, "Unable to load " + os_propfile + ", proceeding ..." );
        }
        final Callback[] v_callback = {
            new NameCallback("Enter username"),
            new PasswordCallback("Enter password", false),
            new TextOutputCallback( TextOutputCallback.INFORMATION, "Please login" )
        };
        
        String s_name = prop_session.getProperty( os_name_key );
        String s_password = prop_session.getProperty( os_password_key );
        String s_session_id = prop_session.getProperty( os_session_key );
        
        if ( s_session_id != null ) {
            try {
                SessionHelper helper = manager.getSessionHelper( UUIDFactory.parseUUID(s_session_id) );
                // Make sure the session hasn't expired by retrieving a service
                AssetRetriever search = helper.getService( ServiceType.ASSET_SEARCH );
                // ok
                return helper;
            } catch ( Exception ex ) {
                olog.log( Level.INFO, "Failed to connect to session: " + s_session_id + ", continueing", ex );
            }
        }
        File fh_home = PropertiesLoader.get ().getLittleHome();        
        
        if ( s_name != null ) {
            ((NameCallback) v_callback[0]).setName( s_name );
        }
        if ( s_password != null ) {
            try {
                SessionHelper helper = manager.login( s_name, s_password, "generic client login" );
                prop_session.setProperty( os_session_key, helper.getSession().getObjectId().toString () );
                if ( null != fh_home ) {
                    try {
                        PropertiesLoader.get().safelySave( prop_session, 
                                new File( fh_home, os_propfile )
                                );
                    } catch ( IOException ex ) {
                        olog.log( Level.INFO, "Failed to save new session info", ex );
                    }
                }
                return helper;
            } catch ( Exception ex ) {
                olog.log( Level.INFO, "lates_session.properties have invalid username/password, continueing", ex );
            }
        }
        
        for ( int i=0; i < i_retry; ++i ) {
            try {
                handler.handle( v_callback );  
                s_name = ((NameCallback) v_callback[0]).getName ();
                s_password = new String( ((PasswordCallback) v_callback[1]).getPassword() );
            } catch ( RuntimeException ex ) {
                throw ex;
            } catch ( IOException ex ) {
                throw ex;
            } catch ( Exception ex ) {
                olog.log( Level.WARNING, "Failed to authenticate to " + SessionUtil.get().getRegistryHost(), 
                        ex );
                throw new FailedLoginException( "Unable to authenticate: " + ex.getMessage() );
            }
            try {
                SessionHelper helper = manager.login( s_name, s_password, "client login" );
                if ( null != fh_home ) {
                    try {
                        prop_session.setProperty( os_name_key, s_name );
                        prop_session.setProperty( os_password_key, s_password );
                        prop_session.setProperty( os_session_key, helper.getSession().getObjectId().toString () );
                        PropertiesLoader.get().safelySave( prop_session, 
                                new File( fh_home, os_propfile )
                                );
                    } catch ( IOException ex ) {
                        olog.log( Level.INFO, "Failed to save session info", ex );
                    }
                }
                return helper;
            } catch ( RuntimeException ex ) {
                throw ex;
            } catch ( Exception ex ) {
                olog.log( Level.INFO, "Failed login attempt " + i, ex );
            } finally {
                v_callback[2] = new TextOutputCallback( TextOutputCallback.ERROR, "Login Failed" );
            }
        }
        throw new FailedLoginException ( "Retires expended" );
    }
    
    /**
     * Authenticate with the registered CallbackHandler if the SessionHelper
     * is not already injected, then setup the client bindings to the
     * littleware services.
     * 
     * @param binder
     */
    public void configure(Binder binder) {
        if ( null == ohelper ) {
            try {
                ohelper = authenticate( SessionUtil.get().getSessionManager (), ohandler, 3 );
            } catch ( Exception ex ) {
                throw new AssertionFailedException ( "Failed to authenticate to " + SessionUtil.get().getRegistryHost(), ex );
            }
        }
        for( ServiceType<? extends Remote> service : ServiceType.getMembers() ) {
            bind( binder, service, ohelper );
        }
        binder.bind( LittleSession.class ).toProvider( new Provider<LittleSession> () {
            public LittleSession get () {
                try {
                    return ohelper.getSession();
                } catch( RuntimeException e ) {
                    throw e;
                } catch ( Exception e ) {
                    throw new AssertionFailedException( "Failed to retrieve active session", e );
                }
            }
        }
        );
        binder.bind( AssetRetriever.class ).toProvider (
                new Provider<AssetRetriever> () {
            public AssetRetriever get () {
                try {
                    return ohelper.getService( ServiceType.ASSET_SEARCH );
                } catch ( Exception e ) {
                    throw new littleware.base.FactoryException( "service retrieval failure", e );
                }
            }
        }
                );        
    }
    
}
