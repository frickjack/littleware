/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.client;

import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberIndex;
import littleware.apps.tracker.ProductManager;
import littleware.apps.tracker.ZipUtil;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.base.feedback.Feedback;

public class SimpleProductManager implements ProductManager {
    private static final Logger log = Logger.getLogger( SimpleProductManager.class.getName() );
    private final BucketManager bucketMan;
    private static final String  zipPath = "memberData.zip";
    private static final String  indexPath = "memberIndex.txt";
    private final AssetManager assetMan;
    private final AssetSearchManager search;
    private final ZipUtil zipUtil;

    @Inject
    public SimpleProductManager( BucketManager bucketMan,
            AssetManager assetMan,
            AssetSearchManager search,
            ZipUtil zipUtil
            ) {
        this.bucketMan = bucketMan;
        this.assetMan = assetMan;
        this.search = search;
        this.zipUtil = zipUtil;
    }

    @Override
    public void checkout(UUID memberId, File destinationFolder,
            Feedback feedback
            ) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Member checkin(UUID versionId, String memberName, File source, String comment,
            Feedback feedback
            ) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        feedback.info( "Setting up meber asset: " + memberName );
        Member member = assetMan.saveAsset(
                Member.MemberType.create().parent(
                    search.getAsset( versionId ).get()
                    ).name( memberName ).build(),
                    "Setup member"
                    );
        feedback.setProgress(2,10);
        feedback.info( "Zipping source: " + source.getAbsolutePath() );
        final ZipUtil.ZipInfo info = zipUtil.zip(source, feedback);
        feedback.setProgress(7,10);
        // TODO - feedback nesting, bucket cache, bucket streaming
        final byte[] zipData = new byte[ (int) info.getZipFile().length() ];
        final InputStream zipIn = new FileInputStream( info.getZipFile() );
        try {
            int position = 0;
            for ( int count = zipIn.read( zipData );
                  count >= 0;
                  count = zipIn.read( zipData, position, zipData.length - position )
                  ) {
                position += count;
            }
        } finally {
            zipIn.close();
        }
        member = bucketMan.writeToBucket(member, zipPath, zipData, 
                        "Saving bucket data to " + zipPath
                        );
        feedback.setProgress(9,10);
        feedback.info( "Saving zip index" );
        member = bucketMan.writeToBucket(member, indexPath, 
                zipUtil.pickle(info.getIndex()), 
                comment);
        feedback.setProgress(10,10);
        return member;
    }

    @Override
    public MemberIndex loadIndex(UUID memberId) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
