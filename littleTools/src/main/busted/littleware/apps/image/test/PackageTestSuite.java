/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.image.test;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.client.test.AssetTestFactory;


public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );
    
    @Inject
    public PackageTestSuite(
            ImageManagerTester  imageTester,
            ImageCacheTester    cacheTester,
            ThumbManagerTester  thumbTester
            ) {
        this.addTest( imageTester );
        this.addTest( cacheTester );
        this.addTest( thumbTester );
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        try {
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            return (new AssetTestFactory()).build( serverBoot,
                ClientBootstrap.clientProvider.get().build(),
                PackageTestSuite.class
                );
        } catch ( RuntimeException ex ) {
            log.log( Level.SEVERE, "Test setup failed", ex );
            throw ex;
        }
    }

    public static void main( String[] ignore ) {
        final String[] args = new String[] { "-noloading", PackageTestSuite.class.getName() };
        junit.swingui.TestRunner.main(args);
    }
}
