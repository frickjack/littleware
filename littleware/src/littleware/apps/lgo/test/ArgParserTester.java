/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import littleware.apps.lgo.AbstractLgoCommand;
import littleware.apps.lgo.LgoException;
import littleware.test.LittleTest;

/**
 * Test AbstractLgoCommand.parseArg
 */
public class ArgParserTester extends LittleTest {

    public ArgParserTester() {
        setName( "testParser" );
    }

    public void testParser() {
        final Map<String,String> mapDefault = new HashMap<String,String>();
        for ( int i=0; i < 10; ++i ) {
            mapDefault.put( Integer.toString( i ), Integer.toString( i ) );
        }
        try {
            {
                final Map<String, String> mapResult = AbstractLgoCommand.processArgs(mapDefault, new ArrayList<String>());
                assertTrue( "Got expected empty-args result size: " + mapResult.size(),
                        mapResult.size() == mapDefault.size()
                        );
            }
            {
                final Map<String, String> mapResult = AbstractLgoCommand.processArgs(mapDefault,
                        Arrays.asList( "-1", "1000", "-2", "2000", "-3", "3000")
                        );
                assertTrue( "Got expected empty-args result size: " + mapResult.size(),
                        mapResult.size() == mapDefault.size()
                        );
                for ( int i=1; i < 4; ++i ) {
                    assertTrue( "Got expected arg value " + i + ": " +
                            mapResult.get( Integer.toString( i ) ),
                            Integer.toString( 1000 * i ).equals( mapResult.get( Integer.toString( i ) ))
                            );
                }
            }

        } catch (LgoException ex) {
            Logger.getLogger(ArgParserTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main( String[] vArgs ) {
        SwingUtilities.invokeLater( new Runnable () {

            @Override
            public void run() {
                junit.swingui.TestRunner.main(
                        new String[] { "-noloading",
                        ArgParserTester.class.getName()
                }
                );
                //junit.textui.TestRunner.main( v_launch_args );
            }
        }
        );

    }
}
