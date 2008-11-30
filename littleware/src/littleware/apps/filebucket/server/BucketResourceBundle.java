package littleware.apps.filebucket.server;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ListResourceBundle;

import littleware.base.BaseException;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetManager;
import littleware.asset.AssetException;
import littleware.apps.filebucket.*;
import littleware.security.auth.ServiceProviderFactory;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.server.AbstractServiceProviderFactory;


/** 
 * Server side resources supporting the littleware.apps.filebucket package
 */
public class BucketResourceBundle  extends ListResourceBundle {
	private static Object[][] ov_contents = {
        { "BucketServiceProvider", null
        }
    };   
    private static boolean ob_initialized = false;

    private static synchronized void initBundle () {
        if ( ob_initialized ) {
            return;
        }
        final AssetSearchManager m_search = null;
        final AssetManager       m_asset = null;
        final BucketManager m_bucket = new SimpleBucketManager ( m_search, m_asset );
        final ServiceProviderFactory<BucketManager> factory_bucket =
            new AbstractServiceProviderFactory<BucketManager> ( BucketServiceType.BUCKET_MANAGER, m_search ) {
                public BucketManager createServiceProvider ( SessionHelper m_helper ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
                    BucketManager m_proxy = checkAccessMakeProxy ( m_helper, false,
                                                                   m_bucket
                                                                   );
                    return new RmiBucketManager ( m_proxy );
                }
            };
        
        ov_contents[0][1] = factory_bucket;
        ob_initialized = true;
    }
           
    /**
     * Implements ListResourceBundle's one abstract method -
	 * ListResourceBundle takes care of the rest of the ResourceBundle interface.
	 */
	public Object[][] getContents() {
		if ( ! ob_initialized ) {
			initBundle ();
		}
		return ov_contents;
	}	
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

