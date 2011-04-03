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


import littleware.asset.Asset;
import littleware.apps.filebucket.Bucket;
import littleware.asset.AssetBuilder;


/**
 * Interface for Comment assets.
 * Hold a summary within the asset data block,
 * and store support files in the 
 * {@link littleware.apps.filebucket.Bucket Bucket}
 * associated with this asset.
 */
public interface Comment extends Asset {

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
    
    public interface CommentBuilder extends AssetBuilder {
        @Override
        public CommentBuilder copy( Asset source );
        @Override
        public CommentBuilder parent( Asset value );

        @Override
        public CommentBuilder name( String value );
        
        /**
         * Also sets summary property
         */
        public String getFullText();
        public void setFullText( String value );
        public CommentBuilder fullText( String value );
        
        @Override
        public Comment build();
    }
}


