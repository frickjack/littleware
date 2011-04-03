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
import com.google.inject.Provider;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.lgo.ListChildrenCommand;
import littleware.asset.AssetType;
import littleware.asset.test.AbstractAssetTest;
import littleware.base.feedback.LoggerFeedback;

/**
 * Test lgo.ListChildrenCommand - just get the children under littleware.test_home
 */
public class ListChildrenTester extends AbstractAssetTest {
    private static final Logger log = Logger.getLogger( ListChildrenTester.class.getName() );

    private final Provider<ListChildrenCommand.Builder> provideCommand;

    @Inject
    ListChildrenTester( Provider<ListChildrenCommand.Builder> provideCommand ) {
        setName( "testListChildren" );
        this.provideCommand = provideCommand;
    }

    public void testListChildren() {
        try {
            final String sResult = provideCommand.get(
                    ).buildFromArgs(
                    Arrays.asList("-path", getTestHome() )
                    ).runCommandLine( new LoggerFeedback() );
            log.log( Level.INFO, "List children under " + getTestHome() + " + got: " + sResult );
            assertTrue( "Found some children under " + getTestHome(),
                    sResult.split( "\n" ).length > 0
                    );
            {
                final ListChildrenCommand command = provideCommand.get(
                    ).buildFromArgs( Arrays.asList( "-path", getTestHome() ));
                final ListChildrenCommand.Input testData = command.getInput();
                assertTrue( "Empty asset-type detected in args parsing", testData.getChildType().isEmpty() );
            }
            {
                final ListChildrenCommand command = provideCommand.get(
                    ).buildFromArgs( Arrays.asList( "-path", getTestHome(),
                                    "-type", AssetType.GENERIC.toString() )
                                    );
                final ListChildrenCommand.Input testData = command.getInput();
                assertTrue( "Generic asset-type detected in args parsing: " + testData.getChildType(),
                        testData.getChildType().isSet() && testData.getChildType().get().equals( AssetType.GENERIC )
                        );
            }

        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }
}
