/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;


import littleware.apps.tracker.Task.TaskBuilder;
import littleware.asset.Asset;
import littleware.asset.SimpleAssetBuilder;


/**
 * Simple implementation of Task
 */
public class SimpleTaskBuilder extends SimpleAssetBuilder implements Task.TaskBuilder {
    private TaskStatus status;

    public SimpleTaskBuilder() {
        super( TrackerAssetType.TASK );
    }

    @Override
    public final void setTaskStatus(TaskStatus value) {
        taskStatus( value );
    }

    @Override
    public SimpleTaskBuilder taskStatus(TaskStatus value) {
        this.status = value;
        return this;
    }

    @Override
    public TaskBuilder parent(Asset value) {
        super.parent( value );
        return this;
    }

    @Override
    public TaskBuilder copy(Asset value) {
        super.copy( value );
        return this;
    }
}