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
import littleware.apps.filebucket.Bucket;
import littleware.asset.AssetType;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.base.UUIDFactory;


/**
 * Interface for Comment assets.
 * Hold a summary within the asset data block,
 * and store support files in the 
 * {@link littleware.apps.filebucket.Bucket Bucket}
 * associated with this asset.
 */
public interface Comment extends TreeNode {

    /**
     * Extracts summary information from the Asset Data block
     */
    public String getSummary ();
    
        
    /**
     * Get the full-text.  The full-text is lazy-load.
     */
    public String getFullText ();
    
    @Override
    public CommentBuilder copy();

    //-------------------------------------------------------------------

    public static final AssetType COMMENT_TYPE = new AssetType(
            UUIDFactory.parseUUID("FB8CC7B7C9324EC8953DE50A700344F3"), "littleware.apps.tracker.COMMENT",
            TreeNode.TREE_NODE_TYPE
            );

    //-------------------------------------------------------------------
    public interface CommentBuilder extends TreeNode.TreeNodeBuilder {
        @Override
        public CommentBuilder copy( Asset source );
        @Override
        public CommentBuilder parent( TreeParent value );

        @Override
        public CommentBuilder name( String value );
        
        /**
         * Also sets summary property
         */
        public String getFullText();
        public void setFullText( String value );
        public CommentBuilder fullText( String value );

        @Override
        public CommentBuilder creatorId(UUID value);

        @Override
        public CommentBuilder lastUpdaterId(UUID value);

        @Override
        public CommentBuilder aclId(UUID value);

        @Override
        public CommentBuilder ownerId(UUID value);

        @Override
        public CommentBuilder comment(String value);

        @Override
        public CommentBuilder lastUpdate(String value);

        @Override
        public CommentBuilder homeId(UUID value);

        @Override
        public CommentBuilder parentId(UUID value);


        @Override
        public CommentBuilder createDate(Date value);

        @Override
        public CommentBuilder lastUpdateDate(Date value);


        @Override
        public CommentBuilder timestamp(long value);

        @Override
        public Comment build();
    }
}


