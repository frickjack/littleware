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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import littleware.apps.tracker.Comment.CommentBuilder;
import littleware.apps.tracker.Queue.QueueBuilder;
import littleware.apps.tracker.Task.TaskBuilder;
import littleware.asset.*;

import littleware.base.UUIDFactory;

/** 
 * AssetType specializer and bucket for littleware.apps.tracker
 * based AssetTypes.  Implements guice Module interface that
 * different littleware modules can delegate to to bind
 * Providers for Task.TaskBuilder, Comment.CommentBuilder, ...
 */
public class TrackerAssetType implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind( Task.TaskBuilder.class ).toProvider( TrackerAssetType.TASK );
        binder.bind( Queue.QueueBuilder.class ).toProvider( TrackerAssetType.QUEUE );
        binder.bind( Comment.CommentBuilder.class ).toProvider( TrackerAssetType.COMMENT );
    }

    public static class CommentAssetType extends AssetType implements Provider<Comment.CommentBuilder> {
        CommentAssetType() {
            super(UUIDFactory.parseUUID("FB8CC7B7C9324EC8953DE50A700344F3"), "littleware.apps.tracker.COMMENT");
        }

        @Override
        public Comment.CommentBuilder create() {
            return new SimpleCommentBuilder();
        }

        @Override
        public CommentBuilder get() {
            return create();
        }
    }
    
    public static final CommentAssetType COMMENT = new CommentAssetType();

    
    public static final AssetType DEPENDENCY = new AssetType(
            UUIDFactory.parseUUID("489F21E1D19B49F3B923E7B45609A811"), "littleware.apps.tracker.DEPENDENCY") {

        @Override
        public AssetBuilder create() {
            throw new UnsupportedOperationException( "Not yet implemented" );
        }
    };


    public static class TaskType extends AssetType implements Provider<Task.TaskBuilder> {
        protected TaskType() {
            super(UUIDFactory.parseUUID("84F04E04DCE947B2A00294949DC38628"),
            "littleware.apps.tracker.TASK");
        }

        @Override
        public Task.TaskBuilder create() {
            return new SimpleTaskBuilder();
        }

        @Override
        public TaskBuilder get() {
            return create();
        }
    }

    public static final TaskType TASK = new TaskType();

    public static class QueueType extends AssetType implements Provider<Queue.QueueBuilder> {
        protected QueueType() {
            super( UUIDFactory.parseUUID("0FE9FBED5F6846E1865526A2BFBC5182"),
            "littleware.apps.tracker.QUEUE");
        }

        @Override
        public Queue.QueueBuilder create() {
            return new SimpleQueueBuilder();
        }

        @Override
        public QueueBuilder get() {
            return create();
        }
    }

    public static final QueueType QUEUE = new QueueType();
}


