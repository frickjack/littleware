/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.test;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.BaseException;

/**
 * Slight specialization of junit.framework.TestCase
 * adds putName method to allow simultaneously setting
 * a TestCase test-method name and register the test-case
 * with a suite:  suite.addTest ( provider.get().putName( "testWhatever" ) )
 */
public abstract class LittleTest extends TestCase {

    /** Lots of tests want to create test assets under littleware.test_home */
	public static String  getTestHome() { return "littleware.test_home"; }

    /**
     * Lots of test need to get the test-home in order to create
     * assets under it.
     */
    public static Asset getTestHome( AssetSearchManager search ) throws BaseException, RemoteException, GeneralSecurityException {
        return search.getByName( getTestHome(), AssetType.HOME ).get();
    }

    /**
     * Lots of tests want to delete a bunch of assets at tearDown time.
     * This method loops over the list of ids, tries to delete each one,
     * and just logs if the delete fails.
     *
     * @param vDelete assets to try to delete
     * @param mgrAsset to delete with
     * @param log to log delete failures to - do not throw exceptions
     */
    public static void deleteTestAssets( List<UUID> vDelete, AssetManager mgrAsset, Logger log ) {
        for( UUID uDelete : vDelete ) {
            try {
                mgrAsset.deleteAsset(uDelete, "cleanup test" );
            } catch ( Exception ex ) {
                log.log( Level.WARNING, "Failed to delete test asset: " + uDelete );
            }
        }
    }

    /**
     * Call setName(s_name) and return this
     * @param s_name of test-method to run
     * @return this
     */
    public LittleTest putName ( String s_name ) {
        setName( s_name );
        return this;
    }

    /**
     * Extension function for TestCase instances not
     * derived from LittleTest.
     * 
     * @param test to setName( s_name ) on
     * @param s_name to assign to test.setName( s_name )
     * @return test after setName( s_name ) call
     */
    public static TestCase putName( TestCase test, String s_name ) {
        test.setName( s_name );
        return test;
    }
}
