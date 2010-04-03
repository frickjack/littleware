/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.FeedbackIterableBuilder;
import littleware.test.LittleTest;

/**
 * Run FeedbackIterator through a simple exercise
 */
public class FbIteratorTester extends LittleTest {
    private static final Logger log = Logger.getLogger( FbIteratorTester.class.getName() );

    private final FeedbackIterableBuilder fbItBuilder;
    private final Feedback defaultFb;

    @Inject
    public FbIteratorTester( FeedbackIterableBuilder fbItBuilder, Feedback defaultFb ) {
        this.fbItBuilder = fbItBuilder;
        this.defaultFb = defaultFb;
        setName( "testFbIterator" );
    }

    public void testFbIterator() {
        final List<Integer> testList = new ArrayList<Integer>();

        for ( int i=0; i < 10; ++ i ) {
            testList.add( new Integer( i ) );
        }
        int progress = 0;
        for( Integer scan : fbItBuilder.build(testList, defaultFb) ) {
            log.log( Level.INFO, "Scanning: " + scan );
            assertTrue( "Progress advancing: " + progress + " less than " + defaultFb.getProgress(),
                    progress < defaultFb.getProgress()
                    );
            progress = defaultFb.getProgress();
        }
    }
}
