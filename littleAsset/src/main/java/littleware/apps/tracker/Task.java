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

import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.Maybe;
import littleware.base.UUIDFactory;
import littleware.security.LittleUser;

/**
 * Just provide an interface for all Tasks to implement
 */
public interface Task extends Asset {

    public TaskStatus getTaskStatus();

    public UUID getQueueId();

    /**
     * Get user this task is currently assigned to if any
     */
    public Option<UUID> getUserId();

    @Override
    public Task.TaskBuilder copy();
    //------------------------------------------------
    public static final AssetType TASK_TYPE = new AssetType(UUIDFactory.parseUUID("84F04E04DCE947B2A00294949DC38628"),
            "littleware.apps.tracker.TASK");

    //-------------------------------------------------------
    public interface TaskBuilder extends AssetBuilder {

        public TaskStatus getTaskStatus();

        public void setTaskStatus(TaskStatus value);

        public TaskBuilder taskStatus(TaskStatus value);

        public UUID getQueueId();

        /**
         * Similar to parent(), but sets from-id null
         */
        public TaskBuilder queue(Queue value);

        /** Note - does not change task-status, just associates task with user */
        public TaskBuilder assignTo(LittleUser user);

        @Override
        public TaskBuilder copy(Asset value);

        @Override
        public Task build();

        @Override
        public TaskBuilder creatorId(UUID value);

        @Override
        public TaskBuilder lastUpdaterId(UUID value);

        @Override
        public TaskBuilder aclId(UUID value);

        @Override
        public TaskBuilder ownerId(UUID value);

        @Override
        public TaskBuilder comment(String value);

        @Override
        public TaskBuilder lastUpdate(String value);

        @Override
        public TaskBuilder homeId(UUID value);

        @Override
        public TaskBuilder createDate(Date value);

        @Override
        public TaskBuilder lastUpdateDate(Date value);


        @Override
        public TaskBuilder timestamp(long value);
    }
}
