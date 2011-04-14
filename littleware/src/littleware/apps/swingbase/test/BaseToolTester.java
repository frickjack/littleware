/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingbase.test;

import com.google.inject.Inject;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.swingbase.controller.SwingBaseTool;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.model.BaseData.BDBuilder;
import littleware.test.LittleTest;

/**
 * Test the BaseTool
 */
public class BaseToolTester extends LittleTest {
    private static final Logger log = Logger.getLogger( BaseToolTester.class.getName() );
    private final BDBuilder builder;
    private final SwingBaseTool tool;

    @Inject
    public BaseToolTester( BaseData.BDBuilder builder, SwingBaseTool tool ) {
        this.builder = builder;
        this.tool = tool;
        setName( "testBaseTool" );
    }

    public void testBaseTool() {
        try {
            final String now = (new Date()).toString();
            final BaseData data = builder.appName( "frickjack" ).version( "v0.0" ).
                    helpUrl( new URL( "http://frickjack.com" ) ).
                    putProp( "bla", now ).build();
            tool.saveProps(data);
            assertTrue( "Property save/load worked ok",
                    now.equals( tool.loadSavedProps(data).get( "bla" ) )
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Caught exception", ex );
            fail( "Caught exception: " + ex );
        }
    }
}
