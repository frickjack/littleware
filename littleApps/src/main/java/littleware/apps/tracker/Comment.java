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

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.io.IOException;

import littleware.base.BaseException;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.apps.filebucket.Bucket;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketException;


/**
 * Interface for Comment assets.
 * Hold a summary within the asset data block,
 * and store support files in the 
 * {@link littleware.apps.filebucket.Bucket Bucket}
 * associated with this asset.
 */
public interface Comment extends Asset {
    /** Get upper bound on summary size */
    public int getMaxSummary();

    /**
     * Extracts summary information from the Asset Data block
     */
    public String getSummary ();
    
    /**
     * Sets up summary info in the data block
     *
     * @exception IllegalArgumentException if summary exceeds 
     *         800 characters
     */
    public void setSummary ( String s_summary );
    
    /**
     * Return true if a non-null comment has been saved
     * via saveComment.  Otherwise just use getSummary.
     */
    public boolean hasComment ();
    
    /**
     * Get the full-comment out of the Bucket.
     *
     * @param m_bucket to acces the data-bucket with
     * @return the full comment, or null if no comment set
     */
    public String getComment ( BucketManager m_bucket 
                           )  throws BaseException, GeneralSecurityException,
                             AssetException, RemoteException, BucketException, IOException;
    
    /**
     * Save a new comment and sync() with the updated asset -
     * set hasComment to true.
     *
     * @param m_bucket to access the data-bucket with
     * @param s_comment to save or null to erase comment
     */
    public void saveComment ( BucketManager m_bucket, String s_comment
                              )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException;
    
    /**
     * Clear the comment out of the Bucket, set hasComment to false,
     * and sync with the updated asset.
     *
     * @param m_bucket to access the data-bucket with
     */
    public void eraseComment ( BucketManager m_bucket
                              )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException;
    
    /**
     * Get the path to the file containing the full comment within the bucket
     * associated with this Comment.
     */
    public String getBucketPath ();    
}


