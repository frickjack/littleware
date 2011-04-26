/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo.test;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.lgo.LgoBrowserCommand;
import littleware.base.EventBarrier;
import littleware.base.Maybe;
import littleware.base.feedback.LoggerFeedback;
import littleware.test.LittleTest;

/**
 *
 * @author pasquini
 */
public class BrowserCommandTest extends LittleTest {
    private static final Logger log = Logger.getLogger( BrowserCommandTest.class.getName() );
    private final LgoBrowserCommand.Builder commandBuilder;

    @Inject
    public BrowserCommandTest( LgoBrowserCommand.Builder commandBuilder ) {
        this.commandBuilder = commandBuilder;
        setName( "testBrowserCommand" );
    }

    public void testBrowserCommand() {
        try {
            final EventBarrier<Option<UUID>> barrier = commandBuilder.buildFromArgs( Arrays.asList( "-path", "/littleware.home") ).runCommand( new LoggerFeedback() );
            // Browser actually launches into background as of 2010/01/28
            assertTrue( "User selected something to pass test", barrier.waitForEventData().isSet() );
        } catch ( Exception ex ) {
            log.log( Level.INFO, "Failed test", ex );
            fail( "Caught " + ex );
        }
    }

}
