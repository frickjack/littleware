/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.internal;

import java.util.UUID;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Task;
import littleware.apps.tracker.Task.TaskBuilder;
import littleware.apps.tracker.TaskStatus;
import littleware.apps.tracker.TrackerAssetType;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.Asset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.Maybe;
import littleware.security.LittleUser;

/**
 * Simple implementation of Task
 */
public class SimpleTaskBuilder extends AbstractAssetBuilder implements Task.TaskBuilder {

    public static final String QueueIdKey = "QueueId";
    private static final TaskStatus[] statusValues = TaskStatus.values();

    private static TaskStatus intToStatus(int state) {
        if ((state >= 0) && (state < statusValues.length)) {
            return statusValues[state];
        }
        return TaskStatus.OTHER;

    }

    @Override
    public Task build() {
        return new SimpleTask( this );
    }

    @Override
    public TaskBuilder assignTo(LittleUser user) {
        if ( null != user ) {
            toId( user.getId() );
        } else {
            toId( null );
        }
        return this;
    }

    public static class SimpleTask extends AbstractAsset implements Task {

        protected SimpleTask() {}
        
        protected SimpleTask(SimpleTaskBuilder builder) {
            super( builder );
        }

        @Override
        public TaskStatus getTaskStatus() {
            return intToStatus(getState());
        }

        @Override
        public UUID getQueueId() {
            return this.getLink(QueueIdKey).get();
        }

        @Override
        public Maybe<UUID> getUserId() {
            return Maybe.emptyIfNull(this.getToId());
        }

        @Override
        public TaskBuilder copy() {
            return new SimpleTaskBuilder().copy(this);
        }
    }

    public SimpleTaskBuilder() {
        super(TrackerAssetType.TASK);
    }

    @Override
    public final void setTaskStatus(TaskStatus value) {
        taskStatus(value);
    }

    @Override
    public SimpleTaskBuilder taskStatus(TaskStatus value) {
        super.state(value.ordinal());
        return this;
    }

    @Override
    public TaskBuilder parent(Asset value) {
        if ( value instanceof Queue ) {
            queue( (Queue) value );
        } else {
            super.parent(value);
        }
        return this;
    }

    @Override
    public TaskStatus getTaskStatus() {
        return intToStatus( getState() );
    }

    @Override
    public UUID getQueueId() {
        return this.getLinkMap().get( QueueIdKey );
    }


    @Override
    public TaskBuilder queue(Queue value) {
        final UUID oldFromId = getFromId();
        super.parent( value );
        this.putLink(QueueIdKey, value.getId() );
        if ( null != oldFromId ) {
            // might be a subtask setup
            setFromId( oldFromId );
        }
        return this;
    }

    @Override
    public TaskBuilder copy(Asset value) {
        super.copy(value);
        return this;
    }
}
