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
import com.google.inject.Provider;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.Product.ProductBuilder;
import littleware.apps.tracker.ProductManager;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.Version.VersionBuilder;
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.LittleHome;
import littleware.asset.client.test.AbstractAssetTest;
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
    private final Provider<ProductBuilder> productProvider;
    private final Provider<VersionBuilder> versionProvider;

    @Inject
    public ProductManagerTester( AssetSearchManager search, AssetManager assetMan,
            ProductManager prodMan, FileUtil fileUtil, Feedback feedback,
            Provider<Product.ProductBuilder> productProvider,
            Provider<Version.VersionBuilder> versionProvider
            ) {
        this.search = search;
        this.assetMan = assetMan;
        this.prodMan = prodMan;
        this.fileUtil = fileUtil;
        this.feedback = feedback;
        this.productProvider = productProvider;
        this.versionProvider = versionProvider;
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
            final LittleHome home = getTestHome( search );
            final Product product = assetMan.saveAsset(
                    productProvider.get().parent(home
                    ).name( "PMTester" + now.getTime()
                    ).build(),
                    "Setup test product"
                    );
            final Version version = assetMan.saveAsset(
                    versionProvider.get().product(product
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
