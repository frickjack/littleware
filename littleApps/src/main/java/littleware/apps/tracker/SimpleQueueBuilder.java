/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.tracker;

import littleware.apps.tracker.Queue.QueueBuilder;
import littleware.asset.Asset;
import littleware.asset.SimpleAssetBuilder;

public class SimpleQueueBuilder extends SimpleAssetBuilder implements Queue.QueueBuilder {

    private static class SimpleQueue extends SimpleAsset implements Queue {

        public SimpleQueue() {}
        public SimpleQueue( SimpleQueueBuilder builder ) {
            super( builder );
        }

        @Override
        public QueueBuilder copy() {
            return super.copy().narrow();
        }

        @Override
        public int getNextTaskNumber() {
            return this.getValue().intValue();
        }
    }


    public SimpleQueueBuilder() {
        super( TrackerAssetType.QUEUE );
        value( 1 );
    }

    
    @Override
    public QueueBuilder copy(Asset value) {
        return super.copy( value ).narrow();
    }

    @Override
    public QueueBuilder parent(Asset value) {
        return super.parent( value ).narrow();
    }

    @Override
    public Queue build() {
        return new SimpleQueue( this );
    }

}
