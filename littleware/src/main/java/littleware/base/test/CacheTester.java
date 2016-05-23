/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.test;

import com.google.inject.Inject;
import littleware.base.cache.Cache;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

/**
 * Test implementations of littleware.base.cache.Cache
 */
public class CacheTester extends TestCase {

    private static final Logger log = Logger.getLogger("littleware.base.test.CacheTester");
    private final Cache<String, Integer> cache;
    private final int ageoutSecs = 10;
    private final int maxSize = 100;

    /**
     * Just calls setName and returns this
     */
    public CacheTester putName(String name) {
        this.setName(name);
        return this;
    }

    /**
     * Constructor just stuffs away the cache to test
     */
    @Inject
    public CacheTester(Cache.Builder cacheBuilder) {
        cache = cacheBuilder.maxAgeSecs(ageoutSecs).maxSize(maxSize).build();
    }

    /** Just flush the cache */
    @Override
    public void setUp() {
        cache.clear();
    }

    /** Just flush the cache */
    @Override
    public void tearDown() {
        setUp();
    }

    /**
     * Run some generic put/get/flush tests
     */
    public void testGeneric() {
        for (int i = 0; i < 10; ++i) {
            String s_key = Integer.toString(i);
            Integer x_value = Integer.valueOf(i);

            assertTrue("Cache should be empty", null == cache.put(s_key, x_value));
        }
        for (int i = 0; i < 10; ++i) {
            String s_key = Integer.toString(i);
            Object x_value = cache.get(s_key);
            assertTrue("Cache should have entry: " + i, null != x_value);
            assertTrue("Value instance of Integer", x_value instanceof Integer);
            assertTrue("Retrieved wrong value for key: " + i, i == ((Integer) x_value).intValue());
        }
        for (int i = 0; i < 10; ++i) {
            String s_key = Integer.toString(i);
            Integer x_value = Integer.valueOf(i + 5);

            assertTrue("Cache should not be empty", null != cache.put(s_key, x_value));
        }
        for (int i = 0; i < 10; ++i) {
            String s_key = Integer.toString(i);
            Object x_value = cache.get(s_key);
            assertTrue("Cache should have entry: " + i, null != x_value);
            assertTrue("Value instance of Integer", x_value instanceof Integer);
            assertTrue("Retrieved wrong 2nd value for key: " + i, i + 5 == ((Integer) x_value).intValue());

            cache.remove(s_key);
            assertTrue("Should have lost entry after flush: " + i, null == cache.get(s_key));
        }
    }

    /**
     * Test that the freakin' thing doesn't go beyond the max-size
     */
    public void testSizeLimit() {
        int i_max = maxSize + 50;

        for (int i = 0; i < i_max; ++i) {
            String s_key = Integer.toString(i);
            Integer x_value = Integer.valueOf(i);

            assertTrue("Cache should be empty", null == cache.put(s_key, x_value));
        }

        int i_hit = 0;
        int i_miss = 0;
        for (int i = 0; i < i_max; ++i) {
            String s_key = Integer.toString(i);
            Object x_value = cache.get(s_key);
            if (null != x_value) {
                ++i_hit;
            } else {
                ++i_miss;
            }
        }

        assertTrue("Cache of size " + maxSize + " maxed out with hits: " + i_hit,
                i_hit > maxSize - 2);
        assertTrue("Cache of size " + maxSize + " grew beyond size limit to: " + i_hit,
                i_miss > 0);
    }

    /**
     * Verify that the cache ages entries out
     */
    public void testAgeOut() {
        int i_max = 10;

        for (int i = 0; i < i_max; ++i) {
            String s_key = Integer.toString(i);
            Integer x_value = Integer.valueOf(i);

            assertTrue("Cache should be empty", null == cache.put(s_key, x_value));
        }

        assertTrue("ageout secs > 0 ? : " + ageoutSecs, ageoutSecs > 0);
        log.log(Level.INFO, "Sleeping to age out cache: " + (ageoutSecs + 5) + " secs");
        try {
            Thread.sleep(1000 * (ageoutSecs + 5));

            for (int i = 0; i < i_max; ++i) {
                String s_key = Integer.toString(i);
                Object x_value = cache.get(s_key);
                assertTrue("Value should have aged out: " + i, null == x_value);
            }
        } catch (InterruptedException e) {
            assertTrue("Test interrupted, caught: " + e, false);
        }
    }
}

