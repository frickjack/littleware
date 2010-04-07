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

import littleware.asset.*;

import littleware.base.UUIDFactory;

/** 
 * AssetType specializer and bucket for littleware.apps.tracker
 * based AssetTypes.
 */
public abstract class TrackerAssetType extends AssetType {

    public static final AssetType COMMENT = new AssetType(
            UUIDFactory.parseUUID("FB8CC7B7C9324EC8953DE50A700344F3"), "littleware.apps.tracker.COMMENT") {

        @Override
        public Comment.CommentBuilder create() {
            return new SimpleCommentBuilder();
        }
    };

    
    public static final AssetType DEPENDENCY = new AssetType(
            UUIDFactory.parseUUID("489F21E1D19B49F3B923E7B45609A811"), "littleware.apps.tracker.DEPENDENCY") {

        @Override
        public AssetBuilder create() {
            throw new UnsupportedOperationException( "Not yet implemented" );
        }
    };


    public static class TaskType extends AssetType {
        protected TaskType() {
            super(UUIDFactory.parseUUID("84F04E04DCE947B2A00294949DC38628"),
            "littleware.apps.tracker.TASK");
        }

        @Override
        public Task.TaskBuilder create() {
            return new SimpleTaskBuilder();
        }
    }
    public static final TaskType TASK = new TaskType();

    public static class QueueType extends AssetType {
        protected QueueType() {
            super( UUIDFactory.parseUUID("0FE9FBED5F6846E1865526A2BFBC5182"),
            "littleware.apps.tracker.QUEUE");
        }

        @Override
        public Queue.QueueBuilder create() {
            return new SimpleQueueBuilder();
        }
    }

    public static final QueueType QUEUE = new QueueType();
}


