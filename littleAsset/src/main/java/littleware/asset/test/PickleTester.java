/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Provider;
import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.GenericAsset.GenericBuilder;


import littleware.base.*;
import littleware.asset.pickle.*;
import littleware.asset.*;
import littleware.test.LittleTest;

/**
 * TestFixture instantiates different littleware.web.beans beans,
 * and exercises them a bit.
 */
public class PickleTester extends LittleTest {
    private static final Logger log = Logger.getLogger(PickleTester.class.getName());

    private final Provider<? extends PickleMaker<Asset>> picklerProvider;
    private final Provider<GenericBuilder> assetProvider;

    /**
     * Constructor stashes PickleType to test against
     */
    public PickleTester(Provider<? extends PickleMaker<Asset>> providePickler,
            Provider<GenericAsset.GenericBuilder> assetProvider
            ) {
        setName("testPickleTwice");
        this.picklerProvider = providePickler;
        this.assetProvider = assetProvider;
    }


    /**
     * Stupid little test - check whether pickling an GenericAsset.GENERIC
     * asset twice yields the same result both times.
     * Not all PickleMaker may require that.
     */
    public void testPickleTwice() {
        try {
            final Asset testAsset = assetProvider.get().
                    name("bogus_pickletest_asset").
                    homeId(UUID.randomUUID()).
                    parentId(UUID.randomUUID()).
                    comment("Test comment").
                    creatorId(UUID.randomUUID()).
                    lastUpdaterId(UUID.randomUUID()).
                    lastUpdate("Bla bla").build();

            final StringWriter stringWriter = new StringWriter();
            picklerProvider.get().pickle(testAsset, stringWriter);
            final String firstPickle = stringWriter.toString();
            log.log(Level.INFO, "First pickle of test asset got: " + firstPickle);

            stringWriter.getBuffer().setLength(0);
            picklerProvider.get().pickle(testAsset, stringWriter);
            final String secondPickle = stringWriter.toString();
            assertTrue("Pickle twice, got same result", firstPickle.equals(secondPickle));
        } catch (Exception ex) {
            log.log(Level.INFO, "Caught unexected: " + ex + ", " +
                    BaseException.getStackTrace(ex));
            assertTrue("Caught unexpected: " + ex, false);
        }
    }
}

