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
import javax.security.auth.login.LoginException;
import littleware.bootstrap.client.ClientBootstrap;

/**
 * Command-line application launcher
 */
public class CLApp {

    private static final Logger log = Logger.getLogger(CLApp.class.getName());

    public static void main(String[] argv) {
        try {
            // automatic authentication uses default remote server specified in
            // ~/.littleware/littleware.properties or localhost
            final ClientBootstrap bootstrap = ClientBootstrap.clientProvider.get().build().automatic();
            final AppBuilder builder = bootstrap.bootstrap(SimpleCLBuilder.class);
            try {
                System.out.println(builder.argv(Arrays.asList(argv)).build().call());
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Command failed", ex);
            } finally {
                bootstrap.shutdown();
            }
        } catch (LoginException ex) {
            log.log(Level.SEVERE, "Failed login", ex);
        }
    }
}
