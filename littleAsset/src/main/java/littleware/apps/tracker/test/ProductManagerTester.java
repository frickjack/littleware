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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductManager;
import littleware.apps.tracker.Version;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.test.AbstractAssetTest;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Test ProductManager checkin/checkout etc.
 */
public class ProductManagerTester extends AbstractAssetTest {
    private static final Logger log = Logger.getLogger( ProductManagerTester.class.getName() );

    private final AssetSearchManager search;
    private final AssetManager assetMan;
    private final ProductManager prodMan;
    private File  testDir;
    private int   testDirSize = 0;
    private final FileUtil fileUtil;
    private final Feedback feedback;

    @Inject
    public ProductManagerTester( AssetSearchManager search, AssetManager assetMan,
            ProductManager prodMan, FileUtil fileUtil, Feedback feedback
            ) {
        this.search = search;
        this.assetMan = assetMan;
        this.prodMan = prodMan;
        this.fileUtil = fileUtil;
        this.feedback = feedback;
        setName( "testProdMan" );
    }

    @Override
    public void setUp() {
        try {
            testDir = new File(
                    Whatever.Folder.Temp.getFolder(),
                    "PMTester" + (new Date()).getTime()
                    );
            testDirSize = fileUtil.buildTestTree(testDir);
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Setup failed", ex );
            fail( "Setup caught exception: " + ex );
        }
    }

    @Override
    public void tearDown() {
        try {
            fileUtil.deleteR(testDir, testDirSize);
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Teardown failed", ex );
            fail( "Teardown caught: " + ex );
        }
    }


    public void testProdMan() {
        try {
            final Date now = new Date();
            final Asset home = getTestHome( search );
            final Product product = assetMan.saveAsset(
                    Product.ProductType.create().parent(home
                    ).name( "PMTester" + now.getTime()
                    ).build(),
                    "Setup test product"
                    );
            final Version version = assetMan.saveAsset(
                    Version.VersionType.create().product(product
                    ).name("1.0.0"
                    ).build(),
                    "Setup test version"
                    );
            log.log( Level.INFO, "Running checkin ..." );
            final Member member = prodMan.checkin(version.getId(), "bla", testDir, "frickjack", feedback);
            final File outputDir = new File(
                    Whatever.Folder.Temp.getFolder(),
                    "checkout" + now.getTime()
                    );
            outputDir.mkdirs();
            log.log( Level.INFO, "Running checkout ..." );
            prodMan.checkout( member.getId(), outputDir, feedback );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Test failed", ex );
            fail( "Caught exception: " + ex );
        }
    }
}
