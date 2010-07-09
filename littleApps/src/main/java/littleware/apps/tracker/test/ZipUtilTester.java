/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.test;

import com.google.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import littleware.apps.tracker.ZipUtil;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Zip and unzip some test data
 */
public class ZipUtilTester extends TestCase {
    private static final Logger log = Logger.getLogger( ZipUtilTester.class.getName() );
    private final ZipUtil util;
    private final Feedback feedback;

    @Inject
    public ZipUtilTester( ZipUtil util, Feedback fb ) {
        super( "testZip" );
        this.util = util;
        this.feedback = fb;
    }

    private File testDir;

    /**
     * Setup a test folder
     */
    @Override
    public void setUp() {
        try {
            File parent = Whatever.Folder.Temp.getFolder();
            int count = 0;
            for ( int dirCount=0; dirCount < 3; ++dirCount ) {
                final File dir = new File( parent, "ZipUtilTester" + count );
                if ( 0 == dirCount ) {
                    testDir = dir;
                }
                count = count + 1;
                parent = dir;
                if ( ! dir.exists() ) {
                    dir.mkdir();
                }                
                for( int fileCount=0; fileCount <= dirCount; ++ fileCount ) {
                    final File file = new File( parent, "ZipUtilTester" + count + ".txt" );
                    count = count + 1;
                    if ( ! file.exists() ) {
                        final Writer writer = new FileWriter( file );
                        try {
                            writer.write("bla bla bla\n" );
                        } finally {
                            writer.close();
                        }
                    }
                }
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed setup", ex );
            fail( "Caught exception: " + ex );
        }
    }

    public void testZip() {
        try {
            final File  testZip = new File( "ZipUtilTester.zip" );
            if ( testZip.exists() ) {
                testZip.delete();
            }
            final ZipUtil.ZipInfo info = util.zip(testDir, testZip, feedback );
            assertTrue( "Got expected zip file",
                    info.getZipFile().equals( testZip )
                    );
            assertTrue( "Zip file is not empty",
                    info.getZipFile().exists() && (info.getZipFile().length() > 0)
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Test failed", ex );
            fail( "Caught exception: " + ex );
        }
    }
}
