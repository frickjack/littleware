/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.test;

import com.google.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import littleware.base.ZipUtil;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Zip and unzip some test data
 */
public class ZipUtilTester extends TestCase {

    private static final Logger log = Logger.getLogger(ZipUtilTester.class.getName());
    private final ZipUtil zipUtil;
    private final Feedback feedback;
    private final FileUtil fileUtil;

    @Inject
    public ZipUtilTester(ZipUtil zipUtil, FileUtil fileUtil, Feedback fb) {
        super("testZip");
        this.zipUtil = zipUtil;
        this.fileUtil = fileUtil;
        this.feedback = fb;
    }

    private File testDir = new File( Whatever.Folder.Temp.getFolder(), "ZipUtilTester");


    /**
     * Setup a test folder
     */
    @Override
    public void setUp() {
        try {
            fileUtil.buildTestTree( testDir );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed setup", ex);
            fail("Caught exception: " + ex);
        }
    }


    public void testZip() {
        try {
            final File testZip = new File("ZipUtilTester.zip");
            if (testZip.exists()) {
                testZip.delete();
            }
            final File info = zipUtil.zip(testDir, testZip, feedback);
            assertTrue("Got expected zip file",
                    info.equals(testZip));
            assertTrue("Zip file is not empty",
                    info.exists() && (info.length() > 0));
            // time to unzip
            final File unzipFolder = new File(Whatever.Folder.Temp.getFolder(), "UnzipTest");
            fileUtil.deleteR( unzipFolder, 20 );
            zipUtil.unzip(info, unzipFolder, feedback);
            final List<File> startList = fileUtil.lsR(testDir);
            final List<File> endList = fileUtil.lsR(unzipFolder);
            int startFileCount = 0;
            int endFileCount = 0;
            int startDirCount = 0;
            int endDirCount = 0;
            for (File scan : startList) {
                if (scan.isFile()) {
                    startFileCount++;
                } else if (scan.isDirectory()) {
                    startDirCount++;
                }
            }
            for (File scan : endList) {
                if (scan.isFile()) {
                    endFileCount++;
                } else if (scan.isDirectory()) {
                    endDirCount++;
                }
            }
            assertTrue("Unzip got correct file count: " + endFileCount + "==" + startFileCount,
                    endFileCount == startFileCount);
            assertTrue("Unzip got correct dir count: " + endDirCount + " vs " + startDirCount,
                    endDirCount == startDirCount + 1 // +1 for parent directory
                    );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        }
    }
}
