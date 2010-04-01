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

import java.util.logging.Logger;
import littleware.apps.tracker.Comment.CommentBuilder;

import littleware.asset.*;

/**
 * Simple implementation of Comment interface.
 */
public class SimpleCommentBuilder extends SimpleAssetBuilder implements Comment.CommentBuilder {
    private static final Logger log = Logger.getLogger( SimpleCommentBuilder.class.getName() );

    private static class SimpleComment extends SimpleAsset implements Comment {

        @Override
        public String getSummary() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFullText() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CommentBuilder copy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    
    private static final String OS_BUCKET_PATH = "comment.txt";
    private String os_summary = null;
    private String fullText = "";

    /** Do nothing constructor */
    public SimpleCommentBuilder() {
        super(TrackerAssetType.COMMENT);
    }

    @Override
    public CommentBuilder copy(Asset source) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommentBuilder parent(Asset value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void setFullText(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommentBuilder fullText(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Comment build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}


