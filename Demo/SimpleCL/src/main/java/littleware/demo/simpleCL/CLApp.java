/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.demo.simpleCL;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.client.ClientBootstrap;

/**
 * Command-line application launcher
 */
public class CLApp {
    private static final Logger log = Logger.getLogger( CLApp.class.getName() );

    public static void main( String[] argv ) {
        final ClientBootstrap bootstrap = new ClientBootstrap( "localhost" );
        final AppBuilder builder = bootstrap.bootstrap( SimpleCLBuilder.class );
        try {
            System.out.println( builder.argv(Arrays.asList(argv)).build().call() );
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Command failed", ex);
        } finally {
            bootstrap.shutdown();
        }
    }
}
