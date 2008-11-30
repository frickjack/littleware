package littleware.apps.filebucket;

import java.util.ResourceBundle;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;

import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.asset.AssetException;
import littleware.base.PropertiesLoader;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.ServiceProviderFactory;

/**
 * Just a namespace to put the BUCKET_SERVICE_TYPE into
 */
public abstract class BucketServiceType extends ServiceType {
    private static ServiceProviderFactory<BucketManager>   ofactory_bucket = null;
    
    public static final ServiceType<BucketManager> BUCKET_MANAGER = 
		new ServiceType<BucketManager> ( UUIDFactory.parseUUID ( "C09675718D7E4ABC8C825D0000CAA0C4" ),
                                         "littleware.BUCKET_MANAGER_SERVICE",
                                         BucketManager.class
                                         )
        {
            /**
             * Server-side service-handler lookup
             */
            public BucketManager createServiceProvider ( SessionHelper m_helper ) throws BaseException, AssetException, 
                GeneralSecurityException, RemoteException
            { 
                if ( null == ofactory_bucket ) {
                    // Access the resource-bundle to get the BucketManager implementation on the server side
                    ResourceBundle bundle_bucket = PropertiesLoader.get().getBundle ( "littleware.apps.filebucket.server.BucketResourceBundle" );
                    ofactory_bucket = (ServiceProviderFactory<BucketManager>) bundle_bucket.getObject ( "BucketServiceProvider" ); 
                }
                return ofactory_bucket.createServiceProvider ( m_helper );
            }
                
        @Override
            public Class<BucketManager> getServiceInterface () { return BucketManager.class; }	
                
        };

}
