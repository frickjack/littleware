package littleware.security.auth.server;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.ResourceBundle;

import javax.security.auth.Subject;

import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.ReadOnlyException;
import littleware.security.auth.*;

/**
 * Base class for server-side ServiceProviderFactory implementations.
 * Provides the checkAccessMakeProxy method - which lets subtypes
 * setup a wrapper around an interface that for each method call
 * takes care of verifying
 * that the client has permissions to access the service method,
 * and does a Subject.doAs() to setup the security context.
 */
public abstract class AbstractServiceProviderFactory<T extends Remote> implements ServiceProviderFactory<T> {
    private final AssetSearchManager    om_search;
    private final ServiceType<T>        on_service;

    
    /**
     * Specify the service this is a factory for, and inject AssetSearchManager
     *
     * @param n_service we are a factory for
     * @param m_search used to verify client access to resource
     */
    public AbstractServiceProviderFactory ( ServiceType<T> n_service,
                                            AssetSearchManager m_search ) {
        on_service = n_service;
        om_search = m_search;
    }
    
    
	/**
     * This method looks up the asset associated with this object's UUID to verify
	 * the active user has permission to access this service.
	 * A good trick is to register the service itself as an Asset in the database
	 * protected by an ACL that can grant/deny access to the entire service.
	 * Once the security checks pass, then this method returns a
	 * SessionInvocationHandler based dynamic proxy wrapping the
	 * given service-provider with a session-aware wrapper.
	 * 
	 * @param m_helper managing the session asking for a new service manager - checks
	 *          for session-expired condition
	 * @param b_readonly_service is this service ok for a read-only session ?
	 *            Set true if any methods are read-only, and let
	 *            the proxy do a per-method check for the @ReadOnly annotation.
	 * @exception AccessDeniedException if service exists, but session-principal
	 *                    does not have read-access to its asset
	 * @exception ReadOnlyException if this is not a read-only service and a_session is
	 *                a read-only session
	 * @exception SessionExpiredException if a_session has expired
	 */
	protected  T checkAccessMakeProxy ( 
                                        SessionHelper m_helper, 
                                        boolean       b_readonly_service,
                                        T             x_provider
										) throws BaseException, AssetException, 
        GeneralSecurityException, RemoteException
    {
        LittleSession a_session = m_helper.getSession ();
        Date          t_now = new Date ();
        
        if ( a_session.getEndDate ().getTime () < t_now.getTime () ) {
            throw new SessionExpiredException ( "Expired at: " + a_session.getEndDate () );
        }
        if ( a_session.isReadOnly () && (! b_readonly_service) ) {
            throw new ReadOnlyException ();
        }
        
        // Service is accessible
        Asset a_service = om_search.getAssetOrNull ( on_service.getObjectId () );
        
        Subject  j_caller = Subject.getSubject ( AccessController.getContext () );
        InvocationHandler  handler_proxy = new SessionInvocationHandler 
            ( j_caller, x_provider, on_service.getCallLogger (), 
              on_service.getCallSampler (),
              m_helper
              );
        Class<T> class_interface = on_service.getServiceInterface ();
        T m_proxy = (T) Proxy.newProxyInstance ( class_interface.getClassLoader (),
                                                 new Class[] { class_interface },
                                                 handler_proxy
                                                 );
        return m_proxy;
    }

    /**
     * Subtypes should create service provider that expects to be
     * running as an authenticated Subject, then calls
     * checkAccessMakeProxy to get a secure proxy,
     * then wraps the proxy in an Rmi-able wrapper.
     */
    public abstract T createServiceProvider ( SessionHelper m_helper ) throws BaseException, AssetException, 
        GeneralSecurityException, RemoteException;
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
