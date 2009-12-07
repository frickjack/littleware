/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.client;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import littleware.asset.AssetException;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.LittleService;
import littleware.asset.client.SimpleLittleService;
import littleware.base.BaseException;
import littleware.security.AccountManager;
import littleware.security.LittleUser;
import littleware.security.Quota;

/**
 * Simple implementation of AccountManagerService
 */
public class SimpleAccountManagerService extends SimpleLittleService implements AccountManagerService {
    private static final long serialVersionUID = 7746369824949101155L;
    private AccountManager oserver;

    /** Default constructor just for serialization support */
    protected SimpleAccountManagerService () {}

    /** Inject the service this object proxies for */
    public SimpleAccountManagerService( AccountManager server ) {
        if ( server instanceof LittleService ) {
            throw new IllegalArgumentException( "Attempt to double wrap service" );
        }
        oserver = server;
    }


    @Override
    public int incrementQuotaCount() throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return oserver.incrementQuotaCount();
    }

    @Override
    public LittleUser createUser(LittleUser p_user, String s_password) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        LittleUser result = oserver.createUser( p_user, s_password );
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public LittleUser updateUser(LittleUser p_update, String s_password, String s_update_comment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        LittleUser result = oserver.updateUser( p_update, s_password, s_update_comment );
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public boolean isValidPassword(String s_password) throws RemoteException {
        return oserver.isValidPassword( s_password );
    }

    @Override
    public LittleUser getAuthenticatedUser() throws GeneralSecurityException, RemoteException {
        LittleUser result = oserver.getAuthenticatedUser();
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public Quota getQuota(LittleUser p_user) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Quota result = oserver.getQuota( p_user );
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

}
