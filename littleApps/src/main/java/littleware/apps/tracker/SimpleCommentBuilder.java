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

import com.google.inject.Inject;

import java.util.logging.Logger;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.tracker.Comment.CommentBuilder;

import littleware.asset.*;
import littleware.base.AssertionFailedException;
import littleware.base.LazyLoadException;
import littleware.base.Maybe;

/**
 * Simple implementation of Comment interface.
 */
public class SimpleCommentBuilder extends SimpleAssetBuilder implements Comment.CommentBuilder {
    private static final Logger log = Logger.getLogger( SimpleCommentBuilder.class.getName() );
    public static final String  fullTextBucketPath = "commentFullText.txt";

    private static class SimpleComment extends SimpleAsset implements Comment {
        private transient BucketManager bucketManager;
        private Maybe<String>   maybeFullText = Maybe.empty();

        public SimpleComment() {}
        public SimpleComment( SimpleCommentBuilder builder ) {
            super( builder );
            maybeFullText = Maybe.something( builder.getFullText() );
        }

        /**
         * Injection point at client-side deserialization time
         * allows lazy-load of full-text comment from server.
         * 
         * @param bucketManager
         */
        @Inject
        public void injectClientServices( BucketManager bucketManager ) {
            this.bucketManager = bucketManager;
        }

        @Override
        public String getSummary() {
            return getData();
        }


        @Override
        public String getFullText() {
            if ( maybeFullText.isEmpty() ) {
                synchronized( this ) {
                    if ( maybeFullText.isEmpty() ) {
                        if ( null == bucketManager ) {
                            throw new AssertionFailedException( "BucketManager not injected on cilent" );
                        }
                        try {
                            maybeFullText = Maybe.something( bucketManager.readTextFromBucket(this.getId(), fullTextBucketPath) );
                        } catch (Exception ex) {
                            throw new LazyLoadException( "Failed to retrieve comment full text", ex );
                        }
                    }
                }
            }
            return maybeFullText.get();
        }

        @Override
        public CommentBuilder copy() {
            return new SimpleCommentBuilder().copy( this );
        }

    }

    private String fullText = "";

    /** Do nothing constructor */
    public SimpleCommentBuilder() {
        super(TrackerAssetType.COMMENT);
    }

    @Override
    public CommentBuilder copy(Asset source) {
        super.copy( source );
        return fullText( ((Comment) source).getFullText() );
    }

    @Override
    public CommentBuilder parent(Asset value) {
        super.parent( value );
        return this;
    }


    @Override
    public String getFullText() {
        return fullText;
    }
    @Override
    public final void setFullText(String value) {
        fullText( value );
    }

    @Override
    public CommentBuilder fullText(String value) {
        fullText = value;
        if ( value.length() > 1000 ) {
            setData( value.substring(0, 1000) + "..." );
        } else {
            setData( value );
        }
        return this;
    }

    @Override
    public Comment build() {
        return new SimpleComment( this );
    }

}


