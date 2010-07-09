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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
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

    private static final Logger log = Logger.getLogger(ZipUtilTester.class.getName());
    private final ZipUtil util;
    private final Feedback feedback;

    @Inject
    public ZipUtilTester(ZipUtil util, Feedback fb) {
        super("testZip");
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
            for (int dirCount = 0; dirCount < 3; ++dirCount) {
                final File dir = new File(parent, "ZipUtilTester" + count);
                if (0 == dirCount) {
                    testDir = dir;
                }
                count = count + 1;
                parent = dir;
                if (!dir.exists()) {
                    dir.mkdir();
                }
                for (int fileCount = 0; fileCount <= dirCount; ++fileCount) {
                    final File file = new File(parent, "ZipUtilTester" + count + ".txt");
                    count = count + 1;
                    if (!file.exists()) {
                        final Writer writer = new FileWriter(file);
                        try {
                            writer.write("bla bla bla\n");
                        } finally {
                            writer.close();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed setup", ex);
            fail("Caught exception: " + ex);
        }
    }

    private List<File> lsR(final File target) {
        final ImmutableList.Builder<File> builder = ImmutableList.builder();
        try {
            return (new Callable<List<File>>() {

                private void call(final File start) {
                    if (!start.exists()) {
                        return;
                    }
                    if (start.isDirectory()) {
                        builder.add(start);
                        for (File scan : start.listFiles()) {
                            call(scan);
                        }
                    } else if (start.isFile()) {
                        builder.add(start);
                    }
                }

                @Override
                public List<File> call() throws Exception {
                    call(target);
                    return builder.build();
                }
            }).call();
        } catch (Exception ex) {
            throw new IllegalStateException("Unexpected", ex);
        }
    }

    public void testZip() {
        try {
            final File testZip = new File("ZipUtilTester.zip");
            if (testZip.exists()) {
                testZip.delete();
            }
            final ZipUtil.ZipInfo info = util.zip(testDir, testZip, feedback);
            assertTrue("Got expected zip file",
                    info.getZipFile().equals(testZip));
            assertTrue("Zip file is not empty",
                    info.getZipFile().exists() && (info.getZipFile().length() > 0));
            // time to unzip
            final File unzipFolder = new File(Whatever.Folder.Temp.getFolder(), "UnzipTest");
            {
                // cleanup test
                final List<File> deleteList = new ArrayList( lsR( unzipFolder ) );
                Collections.reverse( deleteList );
                assertTrue ( "Should be fewer than 12 files to cleanup under " + unzipFolder,
                        deleteList.size() < 12
                        );
                for ( File scan : deleteList ) {
                    scan.delete();
                }
            }
            util.unzip( info.getZipFile(), unzipFolder, feedback );
            final List<File> startList = lsR( testDir );
            final List<File> endList = lsR( unzipFolder );
            int   startFileCount = 0;
            int   endFileCount = 0;
            int   startDirCount = 0;
            int   endDirCount = 0;
            for ( File scan : startList ) {
                if ( scan.isFile() ) {
                    startFileCount++;
                } else if ( scan.isDirectory() ) {
                    startDirCount++;
                }
            }
            for ( File scan : endList ) {
                if ( scan.isFile() ) {
                    endFileCount++;
                } else if ( scan.isDirectory() ) {
                    endDirCount++;
                }
            }
            assertTrue( "Unzip got correct file count: " + endFileCount + "==" + startFileCount,
                    endFileCount == startFileCount
                    );
            assertTrue( "Unzip got correct dir count: " + endDirCount + " vs " + startDirCount,
                    endDirCount == startDirCount + 1  // +1 for parent directory
                    );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        }
    }
}
