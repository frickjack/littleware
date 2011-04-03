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
import littleware.apps.filebucket.BucketUtil;
import littleware.apps.tracker.Comment.CommentBuilder;

import littleware.asset.*;
import littleware.base.AssertionFailedException;
import littleware.base.LazyLoadException;
import littleware.base.Maybe;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.NullFeedback;

/**
 * Simple implementation of Comment interface.
 */
public class SimpleCommentBuilder extends SimpleAssetBuilder implements Comment.CommentBuilder {

    private static final Logger log = Logger.getLogger(SimpleCommentBuilder.class.getName());
    public static final String fullTextBucketPath = "commentFullText.txt";
    private static int cutOffLength = 1000;

    /* Note: must be public for client-side guice injection */
    public static class SimpleComment extends SimpleAsset implements Comment {

        private transient BucketUtil bucketUtil;
        private transient Feedback feedback = new NullFeedback();
        private Maybe<String> maybeFullText = Maybe.empty();

        public SimpleComment() {
        }

        public SimpleComment(SimpleCommentBuilder builder) {
            super(builder);
            maybeFullText = Maybe.something(builder.getFullText());
        }

        /**
         * Injection point at client-side deserialization time
         * allows lazy-load of full-text comment from server.
         * 
         * @param bucketManager
         */
        @Inject
        public void injectClientServices(BucketUtil bucketManager) {
            this.bucketUtil = bucketManager;
        }

        @Override
        public String getSummary() {
            return getData();
        }

        @Override
        public String getFullText() {
            return getData(); // something doesn't work right with RMI below ...
            /*..
            if (maybeFullText.isEmpty()) {
                synchronized (this) {
                    if (maybeFullText.isEmpty()) {
                        if ( true ) { // getData().length() <= cutOffLength) {
                            maybeFullText = Maybe.something(getData());
                        } else {
                            if (null == bucketUtil) {
                                throw new AssertionFailedException("BucketManager not injected on cilent");
                            }
                            if (null == feedback) {
                                throw new AssertionFailedException("Feedback not initialized");
                            }
                            try {
                                maybeFullText = Maybe.something(bucketUtil.readText(this.getId(), fullTextBucketPath, feedback));
                            } catch (Exception ex) {
                                throw new LazyLoadException("Failed to retrieve comment full text", ex);
                            }
                        }
                    }
                }
            }
            return maybeFullText.get();
             * 
             */
        }

        @Override
        public CommentBuilder copy() {
            return new SimpleCommentBuilder().copy(this);
        }
    }
    private String fullText = "";

    /** Do nothing constructor */
    public SimpleCommentBuilder() {
        super(TrackerAssetType.COMMENT);
    }

    @Override
    public CommentBuilder copy(Asset source) {
        super.copy(source);
        return fullText(((Comment) source).getFullText());
    }

    @Override public CommentBuilder name( String value ) {
        super.name( value );
        return this;
    }
    
    @Override
    public CommentBuilder parent(Asset value) {
        super.parent(value);
        return this;
    }

    @Override
    public String getFullText() {
        return fullText;
    }

    @Override
    public final void setFullText(String value) {
        fullText(value);
    }

    @Override
    public CommentBuilder fullText(String value) {
        fullText = value;
        if (value.length() > cutOffLength) {
            throw new UnsupportedOperationException( "Comments currently limited to " + cutOffLength + " characters" );
            //setData(value.substring(0, cutOffLength) + "...");
        } else {
            setData(value);
        }
        return this;
    }

    @Override
    public Comment build() {
        return new SimpleComment(this);
    }
}
