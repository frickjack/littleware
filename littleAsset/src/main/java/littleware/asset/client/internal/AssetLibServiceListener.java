/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.internal;

import littleware.asset.client.AssetLibrary;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.client.spi.AssetDeleteEvent;
import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleListener;


/**
 * Singleton service listener registers itself at OSGi bootstrap
 * time as a SessionHelper service listener,
 * then automatically updates the AssetLibrary based
 * on ServiceEvents fired by LittleServices.
 * Launches background thread that attempts to keep local
 * data in sync with remote repository.
 */
public class AssetLibServiceListener implements LittleListener {
    private static final Logger     log = Logger.getLogger( AssetLibServiceListener.class.getName() );

    private final AssetLibrary libAsset;

    @Inject
    public AssetLibServiceListener( LittleServiceBus bus,
            AssetLibrary libAsset
            ) {
        this.libAsset = libAsset;
        bus.addLittleListener(this);
    }

    @Override
    public void receiveLittleEvent(LittleEvent eventBase ) {
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
