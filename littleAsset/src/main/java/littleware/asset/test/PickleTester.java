/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Provider;
import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;


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

    /**
     * Constructor stashes PickleType to test against
     */
    public PickleTester(Provider<? extends PickleMaker<Asset>> providePickler) {
        setName("testPickleTwice");
        this.picklerProvider = providePickler;
    }


    /**
     * Stupid little test - check whether pickling an GenericAsset.GENERIC
     * asset twice yields the same result both times.
     * Not all PickleMaker may require that.
     */
    public void testPickleTwice() {
        try {
            final Asset testAsset = GenericAsset.GENERIC.create().
                    name("bogus_pickletest_asset").
                    homeId(UUID.randomUUID()).
                    fromId(UUID.randomUUID()).
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

