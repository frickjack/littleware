/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Task;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.AssetInfo;
import littleware.asset.server.NullAssetSpecializer;
import littleware.base.BaseException;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

/**
 * TASK-type specializer manages server-side association of task with queue.
 * The BundleActivator mixin registers the specializer singleton with the
 * specializer registry.
 */
@Singleton
public class SimpleTaskSpecializer extends NullAssetSpecializer {
    private static final Logger log = Logger.getLogger( SimpleTaskSpecializer.class.getName() );

    private final AssetSearchManager search;

    @Inject
    public SimpleTaskSpecializer( AssetSearchManager search )
    {
        this.search = search;
    }


    /**
     * Assign a queue-based name to the asset, and place it in the
     * queue node hierarchy
     */
    @Override
    public void postCreateCallback(Asset asset, AssetManager am) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Task  task = asset.narrow();
        final Queue queue = search.getAsset(task.getQueueId()).get().narrow();

        if ( task.getQueueId() != task.getFromId() ) {
            // TODO - add subtask logic
        }
        am.saveAsset(queue.copy().value( queue.getNextTaskNumber() + 1 ).build(),
                "Advance queue task number"
                );
        final ReadableDateTime now = new DateTime();
        final AssetTreeTemplate template = new AssetTreeTemplate( "Archive",
                new AssetTreeTemplate( Integer.toString( now.getYear() ),
                    new AssetTreeTemplate( now.toString( "MMdd" ) )
                ));
        UUID lastId = null;
        for( AssetInfo info : template.visit(queue, search)) {
            lastId = info.getAsset().getId();
            if ( ! info.getAssetExists() ) {
                am.saveAsset(info.getAsset(), "Setup queue tree" );
            }
        }

        // TODO - add nameUnique() check
        /*
        am.saveAsset( 
                task.copy().fromId(lastId).name( Integer.toString( queue.getNextTaskNumber() ) ).build(),
                "Reposition task in queue asset tree" 
                );
         *
         */
    }

    /**
     * If the name changes - make sure it's unique
     */
    @Override
    public void postUpdateCallback(Asset asset, Asset asset1, AssetManager am) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
