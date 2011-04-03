/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.bootstrap;

import java.io.IOException;

/**
 * Simple command line server
 */
public class CliServer {

    public static void main(String[] args) {
        final ServerBootstrap boot = ServerBootstrap.provider.get().build();
        boot.bootstrap();
        System.out.println("< littleware RMI server bootstrap");
        System.out.println("< hit any key to shutdown");
        System.out.print("> ");
        System.out.flush();
        try {
            System.in.read();
        } catch (IOException ex) {
        }
        System.out.println("< Shutting down... ");
        boot.shutdown();
        System.out.println("< Exiting ... goodbye!");
        System.exit(0);
    }
}
