/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.filebucket.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.io.*;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.filebucket.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.asset.*;
import littleware.asset.server.LittleTransaction;
import littleware.base.AssertionFailedException;
import littleware.base.Whatever;

/**
 * Simple implementation of BucketManager interface.
 * Just stuffs all the assets under a set of roots -
 * select a root for an asset by hasing on the asset id first,
 * but check all roots if bucket not found there.
 */
public class SimpleBucketManager implements BucketManager {

    private static final Logger log = Logger.getLogger(SimpleBucketManager.class.getName());
    private final String[] bucketRoots;
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final Provider<LittleTransaction> provideTrans;

    /** 
     * Contructor takes user-suppled AssetSearchManager
     *
     * @param search to lookup asset info with
     * @param assetMgr to update asset when data added to bucket
     */
    @Inject
    public SimpleBucketManager(AssetSearchManager search, AssetManager assetMgr,
            Provider<LittleTransaction> provideTrans,
            @Named("littleware.bucket.root") String bucketRootIn ) {
        this.search = search;
        this.assetMgr = assetMgr;
        this.provideTrans = provideTrans;
        String bucketRoot = bucketRootIn.trim();
        if ( (! bucketRoot.startsWith( "/" ))
                && (! (new File( bucketRoot )).isAbsolute() )
                && (! bucketRoot.startsWith( "./" ))  // ./ is relaive to current path
                ) {
            // not absolute, no explicitly relative to current path,
            // so make relative to littleware.home
            bucketRoot = Whatever.Folder.LittleHome.getFolder().getAbsolutePath() + "/" + bucketRoot;
        }
        this.bucketRoots = new String[]{
                    bucketRoot + "/Library/LittlewareAssets/Volume1",
                    bucketRoot + "/Library/LittlewareAssets/Volume2",
                    bucketRoot + "/Library/LittlewareAssets/Volume3",
                    bucketRoot + "/Library/LittlewareAssets/Volume4",
                    bucketRoot + "/Library/LittlewareAssets/Volume5",
                    bucketRoot + "/Library/LittlewareAssets/Volume6",
                    bucketRoot + "/Library/LittlewareAssets/Volume7",
                    bucketRoot + "/Library/LittlewareAssets/Volume8",
                    bucketRoot + "/Library/LittlewareAssets/Volume9"
                };
    }

    /** 
     * Simple first-pass system for assigning a bucket-path to an asset.
     * 
     * @param asset Asset that needs a new bucket
     * @return File specifying a directory which may be used as the asset&apos;s bucket -
     *            the directory may not yet exist
     */
    public File getBucketPath(Asset asset) {
        final String s_default_root = bucketRoots[Math.abs(asset.getId().hashCode() % bucketRoots.length)];
        File file_root = new File(s_default_root);

        if (!file_root.exists()) { // check the other roots just 2b safe
            File file_check = null;
            for (String s_root : bucketRoots) {
                file_check = new File(s_root);
                if (file_check.exists()) {
                    file_root = file_check;
                    break;
                }
            }
        }
        return new File(file_root, UUIDFactory.makeCleanString(asset.getId()) + "/userdata");
    }

    @Override
    public Bucket getBucket(UUID assetId) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        final Asset asset = search.getAsset(assetId).get();
        final File bucketFile = getBucketPath(asset);
        final SortedSet<String> memberSet = new TreeSet<String>();

        if (bucketFile.exists()) {
            Collections.addAll(memberSet, bucketFile.list());
        }
        return new SimpleBucket(asset.getId(), memberSet);
    }

    /**
     * Little internal utility - make sure a path does not contain 
     * illegal characters.
     *
     * @param path into bucket to check
     * @exception IllegalBucketPathException if path is illegal
     */
    public void checkBucketPath(String path) throws IllegalBucketPathException {
        if ((null == path)
                || (0 <= path.indexOf('/'))
                || (0 <= path.indexOf('\\'))
                || path.startsWith(".")) {
            throw new IllegalBucketPathException("Illegal path: " + path);
        }
    }

    @Override
    public <T extends Asset> T writeToBucket(T asset, String path,
            byte[] data, long offset, String updateComment) throws BaseException, GeneralSecurityException,
            AssetException, IOException, RemoteException, BucketException {
        checkBucketPath(path);
        final LittleTransaction trans = provideTrans.get();
        final Map<UUID, Asset> v_cache = trans.startDbAccess();
        try {
            // increment transaction count before writing anything - verify write permission
            log.log(Level.FINE, "Writing to bucket {0}, path: {1}", new Object[]{asset, path});
            final Asset result = assetMgr.saveAsset(asset, updateComment);

            final File parentFile = getBucketPath(asset);
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            final File dataFile = new File(parentFile, path);
            final FileOutputStream dataStream = new FileOutputStream(dataFile, true);

            try {
                dataStream.getChannel().position(offset);
                dataStream.write(data);
            } finally {
                dataStream.close();
            }
            return (T) result;
        } finally {
            trans.endDbAccess(v_cache);
        }
    }

    public static class SimpleReadInfo implements ReadInfo, Serializable {
        private  byte[] data;
        private  long offset;
        private  long size;

        /** For serializable */
        public SimpleReadInfo() {}

        public SimpleReadInfo( byte[] data, long offset, long size ) {
            this.data = data;
            this.offset = offset;
            this.size = size;
        }
        @Override
        public byte[] getBuffer() {
            return data;
        }

        @Override
        public long getOffset() {
            return offset;
        }

        @Override
        public long getSize() {
            return size;
        }

    }

    @Override
    public ReadInfo readBytesFromBucket(UUID assetId, String path, long offset, int maxBuf) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        if (maxBuf < 10) {
            throw new IllegalArgumentException("Invalid max buffer: " + maxBuf);
        }
        checkBucketPath(path);
        final LittleTransaction trans = provideTrans.get();
        final Map<UUID, Asset> cache = trans.startDbAccess();
        try {
            final Asset asset = search.getAsset(assetId).get();
            final File dataFile = new File(getBucketPath(asset), path);
            final RandomAccessFile dataStream = new RandomAccessFile(dataFile, "r");
            try {
                final long  fileSize = dataStream.length();
                final byte[] data;
                {
                    int bufSize;
                    if (maxBuf < getMaxBufferSize()) {
                        bufSize = maxBuf;
                    } else {
                        bufSize = getMaxBufferSize();
                    }
                    if ( bufSize > fileSize - offset ) {
                        bufSize = (int) (fileSize - offset);
                    }
                    data = new byte[bufSize];
                }
                
                dataStream.seek(offset);
                if ( dataStream.read(data) < data.length ) {
                    throw new AssertionFailedException( "Failed to fill buffer" );
                }
                return new SimpleReadInfo( data, offset, fileSize );
            } finally {
                dataStream.close();
            }
        } finally {
            trans.endDbAccess(cache);
        }
    }

    @Override
    public <T extends Asset> T eraseFromBucket(T a_bucket, String s_path,
            String s_update_comment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        checkBucketPath(s_path);
        final LittleTransaction trans = provideTrans.get();
        final Map<UUID, Asset> cache = trans.startDbAccess();
        try {
            final File dataFile = new File(getBucketPath(a_bucket), s_path);

            if (!dataFile.exists()) {
                return a_bucket;
            }
            final Asset result = assetMgr.saveAsset(a_bucket, s_update_comment);
            dataFile.delete();
            return (T) result;
        } finally {
            trans.endDbAccess(cache);
        }
    }

    @Override
    public <T extends Asset> T renameFile(T asset, String startPath, String renamePath,
            String updateComment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        checkBucketPath(startPath);
        checkBucketPath(renamePath);
        final LittleTransaction trans = provideTrans.get();
        final Map<UUID, Asset> v_cache = trans.startDbAccess();
        try {
            final File file_data = new File(getBucketPath(asset), startPath);
            final File file_rename = new File(getBucketPath(asset), renamePath);
            if (!file_data.exists()) {
                return asset;
            }
            // verify write permission before doing the rename
            final Asset result = assetMgr.saveAsset(asset, updateComment);
            file_data.renameTo(file_rename);
            return (T) result;
        } finally {
            trans.endDbAccess(v_cache);
        }
    }

    @Override
    public <T extends Asset> T copyFile(UUID sourceId, String sourcePath,
            T destAsset, String destPath,
            String updateComment) throws BaseException, GeneralSecurityException,
            RemoteException, BucketException, IOException {
        final LittleTransaction trans = provideTrans.get();
        final Map<UUID, Asset> cache = trans.startDbAccess();
        final T result = assetMgr.saveAsset(destAsset, updateComment);
        try {
            // Do a read, then a write
            checkBucketPath( sourcePath );
            checkBucketPath( destPath );
            final File inFile = new File(
                    getBucketPath( search.getAsset( sourceId ).get() ),
                    sourcePath
                    );
            final File outFile = new File(
                    getBucketPath( result ),
                    destPath
                    );
            final FileInputStream inStream = new FileInputStream( inFile );
            try {
                final FileOutputStream outStream = new FileOutputStream( outFile );
                try {
                    final FileChannel inChannel = inStream.getChannel();
                    final FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } finally {
                    outStream.close();
                }
            } finally {
                inStream.close();
            }
        } finally {
            trans.endDbAccess(cache);
        }
        return result;
    }

    private final Integer maxBuff = 1024*1024;

    @Override
    public Integer getMaxBufferSize() {
        return maxBuff;
    }
}
