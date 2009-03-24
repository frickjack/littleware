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
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.apps.client.ClientBootstrap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * littleware.apps.lgo package test suite
 */
public class PackageTestSuite extends TestSuite implements BundleActivator {
    private static PackageTestSuite  osingleton = null;

    @Inject
    public PackageTestSuite( Provider<ArgParserTester> factoryArgTester,
            Provider<DeleteAssetTester> factoryDeleteTester,
            Provider<XmlLgoHelpTester> factoryXmlHelpTester,
            Provider<SetImageTester> factoryImageTester,
            Provider<ListChildrenTester> factoryChildrenTester,
            Provider<GetAssetTester> factoryGetTester,
            Provider<CreateFolderTester> factoryCreateTester
            )
    {
        super( PackageTestSuite.class.getName() );
        osingleton = this;

        this.addTest( factoryArgTester.get() );
        this.addTest( factoryDeleteTester.get() );
        this.addTest( factoryXmlHelpTester.get() );
        this.addTest( factoryImageTester.get() );
        this.addTest( factoryChildrenTester.get() );
        this.addTest( factoryGetTester.get() );
        this.addTest( factoryCreateTester.get() );
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        if ( null == osingleton ) {
            throw new IllegalStateException ( "PackageTestSuite not initialized - cannot bootstrap test" );
        }
        return osingleton;
    }

    /**
     * Launch the JUNIT TestRunner
     */
    @Override
    public void start(BundleContext ctx) {
        SwingUtilities.invokeLater( new Runnable () {

            @Override
            public void run() {
                junit.swingui.TestRunner.main(
                        new String[] { "-noloading",
                        PackageTestSuite.class.getName()
                }
                );
                //junit.textui.TestRunner.main( v_launch_args );
            }
        }
        );
    }

    /** NOOP */
    @Override
    public void stop( BundleContext ctx ) {
    }


    public static void main( String[] vArgs ) {
        ClientBootstrap bootstrap = new ClientBootstrap();
        bootstrap.getOSGiActivator().add( PackageTestSuite.class );
        bootstrap.bootstrap();
    }
}
