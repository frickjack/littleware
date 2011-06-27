/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.gson;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.base.BaseException;
import littleware.base.Option;

/**
 * Utility supports resolving nested assets at deserialization time.
 * Client and Server runtimes will have different implementations ...
 */
public interface LittleGsonResolver {
    /**
     * Load the asset with the given id.  Intended for use
     * by a single asset's deserialization process.
     * 
     * @param id
     * @return
     * @throws BaseException
     * @throws GeneralSecurityException
     * @throws RemoteException 
     */
    public Option<Asset> getAsset( UUID id ) throws BaseException, GeneralSecurityException, RemoteException;
    
    /**
     * Register the id of an asset in the process of deserialization to
     * avoid circular loads.
     * 
     * @param id 
     */
    public void markInProcess( UUID id );
    
    /**
     * Null resolver throws UnsupportedOperationException on getAsset call,
     * ignores markInProcess
     */
    public static LittleGsonResolver nullResolver = new LittleGsonResolver() {

        @Override
        public Option<Asset> getAsset(UUID id) throws BaseException, GeneralSecurityException, RemoteException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void markInProcess(UUID id) {
        }
        
    };
}
