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
import littleware.asset.client.ServiceEvent;
import littleware.asset.client.ServiceListener;
import littleware.base.AssertionFailedException;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.client.SessionHelperService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Singleton service listener registers itself at OSGi bootstrap
 * time as a SessionHelper service listener,
 * then automatically updates the AssetModelLibrary based
 * on ServiceEvents fired by LittleServices.
 */
public class AssetModelServiceListener implements ServiceListener, BundleActivator {
    private static final Logger     olog = Logger.getLogger( AssetModelServiceListener.class.getName() );

    private final AssetModelLibrary olibAsset;

    @Inject
    public AssetModelServiceListener( SessionHelper helper,
            AssetModelLibrary libAsset
            ) {
        olibAsset = libAsset;
        if ( ! (helper instanceof LittleService) ) {
            throw new AssertionFailedException( "SessionHelper does not implement LittleService - this bundle only runs in client mode" );
        }
        ((LittleService) helper).addServiceListener(this);
    }

    @Override
    public void receiveServiceEvent(ServiceEvent eventBase ) {
        if ( eventBase instanceof AssetLoadEvent ) {
            final AssetLoadEvent eventLoad = (AssetLoadEvent) eventBase;
            olibAsset.syncAsset( eventLoad.getAsset() );
        } else if ( eventBase instanceof AssetDeleteEvent ) {
            olibAsset.remove( ((AssetDeleteEvent) eventBase).getDeletedId() );
        } else {
            olog.log( Level.WARNING, "Not handling unknown service event of type: " +
                    eventBase.getClass().getName() );
        }
    }

    @Override
    public void start(BundleContext ctx) throws Exception {
        // NOOP - constructor already registered us
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        // NOOP for now
    }

}
