package littleware.security.server;

import java.rmi.RemoteException;
import java.security.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.security.auth.Subject;
import javax.sql.DataSource;

import littleware.base.*;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetException;
import littleware.asset.server.AssetResourceBundle;
import littleware.db.SqlResourceBundle;
import littleware.security.*;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.ServiceProviderFactory;
import littleware.security.auth.server.AbstractServiceProviderFactory;
import littleware.security.auth.server.SimpleSessionManager;


/**
 * Resource bundle under littleware.security package
 */
public class SecurityResourceBundle extends ListResourceBundle {
	private static final Logger         ox_logger = Logger.getLogger ( "littleware.security.server.SecurityResourceBundle" );
	private static final AccountManager om_account;
	private static final AclManager     om_acl;


    /** 
     * Enumerate the ResourceBundle contents
     */
	public enum Content {
        AccountManager,
        AclManager,
        SessionManager,
        AccountServiceProvider,
        AclServiceProvider;
        
        public void set ( Object x_value ) {
            ov_contents[ ordinal () ][1] = x_value;
        }
    }
    
	private static final Object[][] ov_contents = new Object[ Content.values ().length ][2];
    
    static {
        for ( Content n_content: Content.values () ) {
            ov_contents[ n_content.ordinal () ][0] = n_content.toString ();
            n_content.set ( null );
        }
		// Need to make sure the littleware.asset ResourceBundle is initialized first
		AssetResourceBundle bundle_asset = AssetResourceBundle.getBundle ();
		final AssetManager       m_asset = (AssetManager) bundle_asset.getObject ( AssetResourceBundle.Content.AssetManager );
		final AssetSearchManager m_searcher = (AssetSearchManager) bundle_asset.getObject ( AssetResourceBundle.Content.AssetSearcher );
		PrivilegedAction x_action =  new GetGuardedResourceAction ( SqlResourceBundle.getBundle (),
                                                                    SqlResourceBundle.Content.LittlewareConnectionFactory.toString ()
																	);
		
		DataSource sql_factory = (DataSource) AccessController.doPrivileged ( x_action );
		
        // Need to setup AccountManager in AssetResourceBundle due to circular injection dependency with AssetManager
		om_account = (AccountManager) bundle_asset.getObject ( AssetResourceBundle.Content.AccountManager );
		om_acl = new SimpleAclManager ( sql_factory, m_asset, m_searcher );
		
		Content.AccountManager.set ( om_account );
		Content.AclManager.set ( om_acl );				
        Content.AccountServiceProvider.set (
            new AbstractServiceProviderFactory<AccountManager> ( ServiceType.ACCOUNT_MANAGER, m_searcher ) {
                public AccountManager createServiceProvider ( SessionHelper m_helper ) throws BaseException, AssetException, 
                GeneralSecurityException, RemoteException
                { 
                    AccountManager m_proxy = checkAccessMakeProxy ( m_helper, false,
                                                                  om_account
                                                                  );
                    return new RmiAccountManager ( m_proxy ); 
                }                                    
            }
                                            );
        Content.AclServiceProvider.set (
            new AbstractServiceProviderFactory<AclManager> ( ServiceType.ACL_MANAGER, m_searcher ) {
                public AclManager createServiceProvider ( SessionHelper m_helper ) throws BaseException, AssetException, 
                GeneralSecurityException, RemoteException
                { 
                    AclManager m_proxy = checkAccessMakeProxy ( m_helper, false, om_acl );
                    return new RmiAclManager ( m_proxy ); 
                }
            }
                                        );
        
        SimpleSessionManager.setupSingleton ( m_asset, m_searcher ); 
        
        Content.SessionManager.set ( SimpleSessionManager.getManager () );
	}
	
	/** Do nothing constructor */
	public SecurityResourceBundle () {
		super ();
	}
	
	/**
	 * Implements ListResourceBundle's one abstract method -
	 * ListResourceBundle takes care of the rest of the ResourceBundle interface.
	 */
	public Object[][] getContents() {
		/**
		 * Put a SessionManager reference here so SessionUtil 
		 * can load the active SessionManager
		 * via the ResourceBundle mechanism, and avoid 
		 * referencing SimpleSessionManager (a server side class)
		 * in client code
		 */

		return ov_contents;
	}	
	
	/**
	 * Shortcut for easy access to an AccountManager
	 */
	public static AccountManager getAccountManager () {
		return om_account;
	}

    private static   SecurityResourceBundle obundle_singleton = null;
    
    /**
     * Convenience method for server-side clients
     * that can import this class.
     */
    public static SecurityResourceBundle   getBundle () {
        if ( null != obundle_singleton ) {
            return obundle_singleton;
        }

        obundle_singleton = (SecurityResourceBundle) ResourceBundle.getBundle ( "littleware.security.server.SecurityResourceBundle" );
        return obundle_singleton;
    }
    
    /**
     * Provide a Content based getObject method
     */
    public Object getObject ( Content n_content ) {
        return getObject ( n_content.toString () );
    }
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

