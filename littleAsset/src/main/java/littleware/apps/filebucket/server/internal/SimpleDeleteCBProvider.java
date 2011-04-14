/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.filebucket.server.internal;

import com.google.inject.Inject;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.filebucket.server.DeleteCBProvider;
import littleware.asset.Asset;

/**
 * Simple implementation just provides a runner that
 * deletes the bucket associated with some asset
 */
public class SimpleDeleteCBProvider implements DeleteCBProvider {
    private static final Logger olog = Logger.getLogger( SimpleDeleteCBProvider.class.getName() );

    private final SimpleBucketManager omgrBucket;

    @Inject
    public SimpleDeleteCBProvider( SimpleBucketManager mgrBucket ) {
        omgrBucket = mgrBucket;
    }

    private void deleteDirectory( File dir ) {
        try {
            if ( ! dir.exists() ) {
                return;
            }
            if ( dir.isDirectory() ) {
                for( String sChild : dir.list() ) {
                    deleteDirectory( new File( dir, sChild ) );
                }
            }
            dir.delete();
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Bucket cleanup failed for " + dir, ex );
        }
    }

    @Override
    public Runnable build( final Asset aDelete ) {
        final File dirDelete = omgrBucket.getBucketPath(aDelete);
        return new Runnable() {
            @Override
            public void run() {
                 deleteDirectory( omgrBucket.getBucketPath( aDelete ) );
            }
        };
    }

}
