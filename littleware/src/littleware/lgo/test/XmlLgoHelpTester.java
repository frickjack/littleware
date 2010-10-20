/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo.test;

import java.util.logging.Logger;
import junit.framework.*;
import littleware.lgo.LgoHelp;
import littleware.lgo.LgoHelpLoader;
import littleware.lgo.XmlLgoHelpLoader;

/**
 * Verify that the Xml help loader works
 */
public class XmlLgoHelpTester extends TestCase {
    private static final Logger olog = Logger.getLogger( XmlLgoHelpTester.class.getName() );

    /**
     * Constructor sets up testHelpLoad test
     */
    public XmlLgoHelpTester () {
       super( "testHelpLoad" ); 
    }

    /**
     * Test loading a well known help file
     */
    public void testHelpLoad () {
        LgoHelpLoader mgrHelp = new XmlLgoHelpLoader();
        LgoHelp       help = mgrHelp.loadHelp( "littleware.lgo.EzHelpCommand" );
        assertTrue( "Able to load EzHelpCommand help info", null != help );
    }
}
