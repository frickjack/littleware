/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.client;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.client.AssetDeleteEvent;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.LittleService;
import littleware.asset.client.LittleServiceEvent;
import littleware.asset.client.LittleServiceListener;
import littleware.base.AssertionFailedException;
import littleware.security.auth.SessionHelper;

/**
 * Singleton service listener registers itself at OSGi bootstrap
 * time as a SessionHelper service listener,
 * then automatically updates the AssetModelLibrary based
 * on ServiceEvents fired by LittleServices.
 * Launches background thread that attempts to keep local
 * data in sync with remote repository.
 */
public class AssetModelServiceListener implements LittleServiceListener {
    private static final Logger     log = Logger.getLogger( AssetModelServiceListener.class.getName() );

    private final AssetModelLibrary libAsset;

    @Inject
    public AssetModelServiceListener( SessionHelper helper,
            AssetModelLibrary libAsset
            ) {
        this.libAsset = libAsset;
        if ( ! (helper instanceof LittleService) ) {
            throw new AssertionFailedException( "SessionHelper does not implement LittleService - this bundle only runs in client mode" );
        }
        ((LittleService) helper).addServiceListener(this);
    }

    @Override
    public void receiveServiceEvent(LittleServiceEvent eventBase ) {
        if ( eventBase instanceof AssetLoadEvent ) {
            final AssetLoadEvent eventLoad = (AssetLoadEvent) eventBase;
            libAsset.syncAsset( eventLoad.getAsset() );
        } else if ( eventBase instanceof AssetDeleteEvent ) {
            libAsset.assetDeleted( ((AssetDeleteEvent) eventBase).getDeletedId() );
        } else {
            log.log( Level.WARNING, "Not handling unknown service event of type: " +
                    eventBase.getClass().getName() );
        }
    }

}
