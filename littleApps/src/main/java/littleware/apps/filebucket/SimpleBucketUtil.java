/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.filebucket;

import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;


public class SimpleBucketUtil implements BucketUtil {
    private final BucketManager bucketMgr;
    
    @Inject
    public SimpleBucketUtil( BucketManager bucketMgr
            ) {
        this.bucketMgr = bucketMgr;
    }

    @Override
    public <T extends Asset> T writeText(T asset, String bucketPath, String data, String updateComment, Feedback feedback) throws BaseException, GeneralSecurityException, AssetException, RemoteException, IOException {
        return writeAll( asset, bucketPath, data.getBytes( Whatever.UTF8.toString() ), updateComment, feedback );
    }

    @Override
    public String readText(UUID assetId, String path, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void readToFile(UUID assetId, String bucketPath, File destination) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends Asset> T writeAll(T asset, String bucketPath, byte[] data, String updateComment, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] readAll(UUID assetId, String bucketPath, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        final int maxBuff = bucketMgr.getMaxBufferSize();
        final BucketManager.ReadInfo info = bucketMgr.readBytesFromBucket(assetId, bucketPath, 0, maxBuff );
        if ( info.getSize() > 104857600 ) {
            throw new IllegalStateException( "read requires > 100MB buffer" );
        }
        if ( info.getBuffer().length < info.getSize() ) {
            final byte[] buffer = new byte[ (int) info.getSize() ];
            int count = info.getBuffer().length;
            for( BucketManager.ReadInfo next = bucketMgr.readBytesFromBucket(assetId, bucketPath, count, maxBuff );
                count < info.getSize();
                next = bucketMgr.readBytesFromBucket(assetId, bucketPath, count, maxBuff )
            ) {
                final byte[] newData = next.getBuffer();
                for( int i=0; i < newData.length; ++i ) {
                    buffer[ count ] = newData[i];
                    ++count;
                }
                count += next.getBuffer().length;
            }
            return buffer;
        } else {
            return info.getBuffer();
        }
    }

    @Override
    public <T extends Asset> T writeFile(T asset, String bucketPath, File source, String updateComment, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
