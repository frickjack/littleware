/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.bootstrap.AppBootstrap;
import littleware.test.TestFactory;

/**
 * littleware.apps.lgo package test suite
 */
public class PackageTestSuite extends TestSuite {

    @Inject
    public PackageTestSuite( Provider<ArgParserTester> factoryArgTester,
            Provider<XmlLgoHelpTester> factoryXmlHelpTester
            )
    {
        setName( PackageTestSuite.class.getName() );

        if ( true ) {
            this.addTest( factoryArgTester.get() );
            this.addTest( factoryXmlHelpTester.get() );
        }
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        return (new TestFactory()).build(
                AppBootstrap.appProvider.get().build(),
                PackageTestSuite.class
                );
    }

    public static void main( String[] args ) {
        junit.swingui.TestRunner.main( new String[] {
            "-noloading",
            PackageTestSuite.class.getName()
            }
        );
    }

}
