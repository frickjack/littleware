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


import littleware.asset.Asset;
import littleware.asset.AssetBuilder;


/**
 * Interface for Queue assets.
 * The task-set and iterator methods are lazy-load wrappers
 * around calls to TaskManager
 */
public interface Queue extends Asset {
    /**
     * Integer used to name tasks in queue - increments
     * whenever task added to queue.
     * Task-engine enforces that every task in queue has a unique number.
     */
    public int  getNextTaskNumber();

    @Override
    public QueueBuilder copy();

    public interface QueueBuilder extends AssetBuilder {
        @Override
        public QueueBuilder copy( Asset value );
        @Override
        public QueueBuilder parent( Asset value );
        @Override
        public Queue build();
    }
}

