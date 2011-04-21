/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;


import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.base.UUIDFactory;


/**
 * Interface for Queue assets.
 * The task-set and iterator methods are lazy-load wrappers
 * around calls to TaskManager
 */
public interface Queue extends TreeNode {
    /**
     * Integer used to name tasks in queue - increments
     * whenever task added to queue.
     * Task-engine enforces that every task in queue has a unique number.
     */
    public int  getNextTaskNumber();

    @Override
    public QueueBuilder copy();

    //-------------------------------------------------------
    public static final AssetType QUEUE_TYPE = new AssetType( UUIDFactory.parseUUID("0FE9FBED5F6846E1865526A2BFBC5182"),
            "littleware.apps.tracker.QUEUE", TreeNode.TREE_NODE_TYPE );

    //-----------------------------------------------------
    
    public interface QueueBuilder extends TreeNode.TreeNodeBuilder {
        @Override
        public QueueBuilder copy( Asset value );
        @Override
        public QueueBuilder parent( TreeParent value );
        public int getNextTaskNumber();
        public void setNextTaskNumber( int value );
        public QueueBuilder nextTaskNumber( int value );
        @Override
        public Queue build();

        @Override
        public QueueBuilder creatorId(UUID value);

        @Override
        public QueueBuilder lastUpdaterId(UUID value);

        @Override
        public QueueBuilder aclId(UUID value);

        @Override
        public QueueBuilder ownerId(UUID value);

        @Override
        public QueueBuilder comment(String value);

        @Override
        public QueueBuilder lastUpdate(String value);

        @Override
        public QueueBuilder homeId(UUID value);

        @Override
        public QueueBuilder parentId(UUID value);


        @Override
        public QueueBuilder createDate(Date value);

        @Override
        public QueueBuilder lastUpdateDate(Date value);


        @Override
        public QueueBuilder timestamp(long value);

    }
}

