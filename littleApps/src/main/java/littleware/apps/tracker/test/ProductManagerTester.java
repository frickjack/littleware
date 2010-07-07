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
import java.util.logging.Logger;
import littleware.apps.tracker.ProductManager;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.test.LittleTest;

/**
 * Test ProductManager checkin/checkout etc.
 */
public class ProductManagerTester extends LittleTest {
    private static final Logger log = Logger.getLogger( ProductManagerTester.class.getName() );

    private final AssetSearchManager search;
    private final AssetManager assetMan;
    private final ProductManager prodMan;

    @Inject
    public ProductManagerTester( AssetSearchManager search, AssetManager assetMan,
            ProductManager prodMan
            ) {
        this.search = search;
        this.assetMan = assetMan;
        this.prodMan = prodMan;
        setName( "testProdMan" );
    }

    public void testProdMan() {
        
    }
}
