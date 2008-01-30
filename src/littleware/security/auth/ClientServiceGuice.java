/*
 * Copyright (c) 2008 Reuben Pasquini
 * All Rights Reserved
 */

package littleware.security.auth;

import java.rmi.Remote;
import java.util.logging.Logger;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;

import littleware.asset.AssetRetriever;
import littleware.base.AssertionFailedException;


/**
 * Bind the implementation of each 
 *    ServiceType.getMembers()
 * interface class
 * to SessionHelper.getService( n_type );
 * This module must be initialized with the SessionHelper
 * from which to acquire service implementations.
 * Also binds LittleSession to helper.getSession
 */
public class ClientServiceGuice implements Module {
    private static final Logger    olog = Logger.getLogger( ClientServiceGuice.class.getName() );
    private final SessionHelper    ohelper;

    public ClientServiceGuice( SessionHelper helper ) {
        ohelper = helper;
    }
    
    /**
     * Get the SessionHelper backing this module     
     */
    public SessionHelper getHelper () {
        return ohelper;
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
    
    public void configure(Binder binder) {
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
