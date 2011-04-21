/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.tracker.internal;

import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Queue.QueueBuilder;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;

public class SimpleQueueBuilder extends AbstractAssetBuilder<Queue.QueueBuilder> implements Queue.QueueBuilder {

    @Override
    public int getNextTaskNumber() {
        return getValue().intValue();
    }

    @Override
    public void setNextTaskNumber(int value) {
        nextTaskNumber( value );
    }

    @Override
    public QueueBuilder nextTaskNumber(int value) {
        return value( value );
    }

    private static class SimpleQueue extends AbstractAsset implements Queue {

        public SimpleQueue() {}
        public SimpleQueue( SimpleQueueBuilder builder ) {
            super( builder );
        }

        @Override
        public int getNextTaskNumber() {
            return this.getValue().intValue();
        }

        @Override
        public QueueBuilder copy() {
            return (new SimpleQueueBuilder()).copy( this );
        }
    }


    public SimpleQueueBuilder() {
        super( Queue.QUEUE_TYPE );
        value( 1 );
    }

    

    @Override
    public Queue build() {
        return new SimpleQueue( this );
    }

}
