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
 * Just provide an interface for all Tasks to implement
 */
public interface Task extends Asset {

    public TaskStatus getTaskStatus();

    @Override
    public Task.TaskBuilder copy();

    public interface TaskBuilder extends AssetBuilder {

        /**
         * Set the task status.
         *
         * @exception TaskStatusException if TaskStatus.MERGED.equals ( n_status ) -
         *                must use mergeWithTask to set that
         */
        public void setTaskStatus(TaskStatus value);
        public TaskBuilder taskStatus( TaskStatus value );

    }
}


