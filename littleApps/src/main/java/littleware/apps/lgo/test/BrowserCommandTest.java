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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.lgo.LgoBrowserCommand;
import littleware.base.Maybe;
import littleware.test.LittleTest;

/**
 *
 * @author pasquini
 */
public class BrowserCommandTest extends LittleTest {
    private static final Logger log = Logger.getLogger( BrowserCommandTest.class.getName() );
    private final LgoBrowserCommand command;

    @Inject
    public BrowserCommandTest( LgoBrowserCommand command ) {
        this.command = command;
        setName( "testBrowserCommand" );
    }

    public void testBrowserCommand() {
        try {
            final Maybe<UUID> maybe = command.runCommand( new LoggerUiFeedback(), "/littleware.home" );
            assertTrue( "User selected something to pass test", maybe.isSet() );
        } catch ( Exception ex ) {
            log.log( Level.INFO, "Failed test", ex );
            fail( "Caught " + ex );
        }
    }

}
