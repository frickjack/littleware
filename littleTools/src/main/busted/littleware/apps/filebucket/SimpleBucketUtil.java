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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.AssertionFailedException;
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
    public void readToFile(UUID assetId, String bucketPath, File destination,
            Feedback feedback
            ) throws BaseException, GeneralSecurityException, RemoteException, IOException
    {
        if ( destination.exists() ) {
            throw new IllegalArgumentException( "Destination already exists: " + destination );
        }
        {
            final File parent = destination.getParentFile();
            if ( (! parent.exists()) || (! parent.isDirectory()) ) {
                throw new IllegalArgumentException( "Parent directory does not exist: " + parent );
            }
        }
        final OutputStream out = new FileOutputStream( destination );
        try {
            readToStream( out, assetId, bucketPath, feedback );
        } finally {
            out.close();
        }
    }

    public void readToStream(OutputStream out, UUID assetId, String bucketPath, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        final int maxBuff = bucketMgr.getMaxBufferSize();
        final BucketManager.ReadInfo info = bucketMgr.readBytesFromBucket(assetId, bucketPath, 0, maxBuff );
        if ( info.getSize() > 104857600 ) {
            throw new IllegalStateException( "read requires > 100MB buffer" );
        }
        out.write( info.getBuffer() );
        if ( info.getBuffer().length < info.getSize() ) {
            long count = info.getBuffer().length;
            for( BucketManager.ReadInfo next = bucketMgr.readBytesFromBucket(assetId, bucketPath, count, maxBuff );
                count < info.getSize();
                next = bucketMgr.readBytesFromBucket(assetId, bucketPath, count, maxBuff )
            ) {
                final byte[] newData = next.getBuffer();
                out.write(newData );
                count += newData.length;
                feedback.setProgress((int) (100 * count / info.getSize()), 100 );
            }
        } 
    }


    @Override
    public byte[] readAll(UUID assetId, String bucketPath, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream( bucketMgr.getMaxBufferSize() );
        readToStream( out, assetId, bucketPath, feedback );
        return out.toByteArray();
    }

    @Override
    public String readText(UUID assetId, String path, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        return new String( readAll( assetId, path, feedback ), Whatever.UTF8 );
    }

    @Override
    public <T extends Asset> T writeText(T asset, String bucketPath, String data, String updateComment, Feedback feedback) throws BaseException, GeneralSecurityException, AssetException, RemoteException, IOException {
        return writeAll( asset, bucketPath, data.getBytes( Whatever.UTF8.toString() ), updateComment, feedback );
    }


    @Override
    public <T extends Asset> T writeAll(T asset, String bucketPath, byte[] data, String updateComment, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        final int maxBuff = bucketMgr.getMaxBufferSize();
        if ( data.length < maxBuff ) {
            return bucketMgr.writeToBucket(asset, bucketPath, data, maxBuff, updateComment);
        }
        final ByteArrayInputStream in = new ByteArrayInputStream( data );
        return writeFromStream( in, data.length, asset, bucketPath, updateComment, feedback );
    }

    public <T extends Asset> T writeFromStream( InputStream in, long totalSize, T asset, String bucketPath, String updateComment, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        final byte[] buffer = new byte[ bucketMgr.getMaxBufferSize() ];
        long count = 0;
        int offset = 0;
        int chunk = 0;
        T result = asset;
        for ( chunk = in.read( buffer );
            chunk >= 0;
            chunk = in.read( buffer, offset, buffer.length - offset )
            ) {
            if ( chunk + offset < buffer.length ) {
                offset += chunk;
            } else {
                offset = 0;
                result = bucketMgr.writeToBucket(result, bucketPath, buffer, count, updateComment);
                count += buffer.length;
                feedback.setProgress( (int) (100L * count / totalSize), 100 );
            }
        }
        if ( offset > 0 ) {
            result = bucketMgr.writeToBucket(result, bucketPath,
                    Arrays.copyOf(buffer, offset),
                    count, updateComment
                    );
            count += offset;
        }
        if ( asset == result ) {
            throw new AssertionFailedException( "No result prepared" );
        }
        return result;
    }
    
    @Override
    public <T extends Asset> T writeFromFile(T asset, String bucketPath, File source, String updateComment, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        if ( ! (
                source.exists()
                && source.isFile()
                )) {
            throw new IllegalArgumentException( "Invalid source file: " + source );
        }
        final InputStream in = new FileInputStream( source );
        try {
            return writeFromStream( in, source.length(), asset, bucketPath, updateComment, feedback );
        } finally {
            in.close();
        }
    }
    

}
