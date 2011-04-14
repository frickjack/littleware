/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.stat.test;

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;

import junit.framework.*;

import littleware.base.stat.*;

/**
 * Test case for littleware.base.stat.Sampler
 */
public class SamplerTester extends TestCase {

    private static final Logger log = Logger.getLogger(SamplerTester.class.getName());
    private final Sampler sampler;

    /**
     * Constructor stashes Sampler implementation to test against
     *
     * @param stat_sampler to test against
     */
    @Inject
    public SamplerTester(Sampler stat_sampler) {
        super("testSampler");
        sampler = stat_sampler;
    }


    /**
     * Just run the Sampler implementation through some basic stat collectin
     */
    public void testSampler() {
        long l_sleep_ms = 100L;

        Random rand_gen = new Random();
        try {
            Date t_start = new Date();
            for (int i = 0; i < 100; ++i) {
                try {
                    Thread.sleep(l_sleep_ms);
                } catch (InterruptedException e) {
                }

                assertTrue("Got " + i + " samples: " + sampler.getNumSamples(),
                        sampler.getNumSamples() == i);
                float f_sample = (float) (rand_gen.nextFloat() * 100.0 + 100.0);
                sampler.sample(f_sample);
                double d_average = sampler.getSampleMean();
                assertTrue("Sample mean is reasonable: " + d_average,
                        (d_average >= 100) && (d_average <= 200));
            }
            double d_variance = sampler.getSampleVariance();
            assertTrue("Sample variance is reasonable: " + d_variance,
                    (d_variance > 100) && (d_variance < 2500));
            assertTrue("Updated start date",
                    sampler.getFirstSampleTime().getTime() > t_start.getTime());
            assertTrue("Updated last sample date",
                    sampler.getLastSampleTime().getTime() > t_start.getTime());
            assertTrue("Tracked arrival max ms: " + sampler.getArrivalMaxMs(),
                    sampler.getArrivalMaxMs() >= l_sleep_ms);
            {
                long l_ms = sampler.getArrivalMinMs();

                assertTrue("Tracked arrival min ms: " + l_ms,
                        (l_ms <= sampler.getArrivalMaxMs())
                        && (l_ms > 0));
            }
            {
                double d_ms = sampler.getArrivalMean();
                assertTrue("Tracked arrival mean: " + d_ms,
                        (d_ms > l_sleep_ms - 10) && (d_ms < l_sleep_ms + 10));
            }
        } finally {
            sampler.clear();
        }
    }
}
