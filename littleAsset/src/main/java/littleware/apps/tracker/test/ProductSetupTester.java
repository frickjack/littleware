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
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberAlias;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductAlias;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.VersionAlias;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.AssetInfo;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.test.AbstractAssetTest;

/**
 * Basic product node-creation test
 */
public class ProductSetupTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(ProductSetupTester.class.getName());
    private final String productName = "SetupTest" + (new Date()).getTime();
    private Asset testFolder;
    private final AssetSearchManager search;
    private final AssetManager assetMan;
    private final TemplateBuilder treeBuilder;

    @Inject
    public ProductSetupTester(AssetSearchManager search, AssetManager assetMan,
            AssetTreeTemplate.TemplateBuilder treeBuilder
            ) {
        setName("testProductSetup");
        this.search = search;
        this.assetMan = assetMan;
        this.treeBuilder = treeBuilder;
    }

    @Override
    public void setUp() {
        try {
            final AssetTreeTemplate template = treeBuilder.assetBuilder("ProductSetupTester").build();
            for( AssetInfo info: template.visit(getTestHome(search), search) ) {
                if ( ! info.getAssetExists() ) {
                    testFolder = assetMan.saveAsset( info.getAsset(), "setup test folder" );
                } else {
                    testFolder = info.getAsset();
                }
            }
        } catch ( Exception ex ) {
            log.log(Level.WARNING, "Failed setup", ex);
            fail("Caught exception: " + ex);
        }
    }

    public void testProductSetup() {
        try {
            final Product product; {
                final Product.ProductBuilder builder = Product.ProductType.create();
                product = assetMan.saveAsset( builder.name( productName ).parent( testFolder ).build(),
                        "test product creation"
                        );
            }
            {
                final ProductAlias.PABuilder builder = ProductAlias.PAType.create();
                assetMan.saveAsset( builder.name( "testAlias" ).parent(product).product(product).build(),
                        "Setup test alias"
                        );
            }
            final Version version; {
                final Version.VersionBuilder builder = Version.VersionType.create();
                version = assetMan.saveAsset( builder.name( "1.0" ).product(product).build(),
                        "Setup test version"
                        );
            }
            {
                final VersionAlias.VABuilder builder = VersionAlias.VAType.create();
                assetMan.saveAsset( builder.name( "versionAlias" ).product(product).version(version).build(),
                        "Test alias"
                        );
            }
            final Member member; {
                final Member.MemberBuilder builder = Member.MemberType.create();
                member = assetMan.saveAsset(
                        builder.name( "member" ).parent( version ).build(),
                        "Test member"
                        );
            }
            {
                final MemberAlias.MABuilder builder = MemberAlias.MAType.create();
                assetMan.saveAsset(
                        builder.name( "memberAlias" ).version(version).member(member).build(),
                        "test alias"
                        );
            }
            {
                final Map<String,UUID> infoMap = product.getDepends();
                assertTrue( "Product getDepends ok: " + infoMap.isEmpty(),
                        (! infoMap.isEmpty()) && (null != infoMap.get("testAlias" ))
                        );
            }
            {
                final Map<String,UUID> versionMap = product.getVersions();
                assertTrue( "Product getVersions ok: " + versionMap.isEmpty(),
                        (! versionMap.isEmpty()) && (null != versionMap.get( "1.0" ))
                        );
            }
            {
                final Map<String,UUID> vaMap = product.getVersionAliases();
                assertTrue( "Version aliases ok: " + vaMap.isEmpty(),
                        vaMap.containsKey("versionAlias" )
                        );
            }
            {
                final Map<String,UUID> memberMap = version.getMembers();
                assertTrue( "Member list ok: " + memberMap.isEmpty(),
                        memberMap.containsKey("member" )
                        );
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Test failed", ex );
            fail( "Test failed: " + ex );
        }
    }
}
