package littleware.asset.server.bootstrap;

import java.io.IOException;

import littleware.bootstrap.LittleBootstrap;

/**
 * Simple command line server
 */
public class CliServer {

    public static void main(String[] args) {
        final LittleBootstrap boot = LittleBootstrap.factory.lookup(LittleBootstrap.class);
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
