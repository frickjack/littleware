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


import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.base.Maybe;

/**
 * Just provide an interface for all Tasks to implement
 */
public interface Task extends Asset {

    public TaskStatus getTaskStatus();
    public UUID       getQueueId();
    /**
     * Get user this task is currently assigned to if any
     */
    public Maybe<UUID>  getUserId();
    

    @Override
    public Task.TaskBuilder copy();

    public interface TaskBuilder extends AssetBuilder {

        public TaskStatus getTaskStatus();
        public void setTaskStatus(TaskStatus value);
        public TaskBuilder taskStatus( TaskStatus value );

        public UUID  getQueueId();

        /**
         * Similar to parent(), but sets from-id null
         */
        public TaskBuilder queue( Queue value );

        @Override
        public TaskBuilder parent( Asset value );
        @Override
        public TaskBuilder copy( Asset value );

    }
}


