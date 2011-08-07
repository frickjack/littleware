/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.net;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.io.Serializable;
import java.rmi.server.RMISocketFactory;
import java.util.logging.Logger;

/**
 * Custom socket factory - just calls through to default factory,
 * but can add hooks.
 */
public class LittleRMISocketFactory implements Serializable, RMIClientSocketFactory {
    private static final Logger log = Logger.getLogger( LittleRMISocketFactory.class.getName () );
    private final static RMIClientSocketFactory       defaultFactory =
            (null == RMISocketFactory.getSocketFactory()) ?
                RMISocketFactory.getDefaultSocketFactory()
                : RMISocketFactory.getSocketFactory ()
                ;
    //private final static RMIClientSocketFactory       cgiFactory = new sun.rmi.transport.proxy.RMIHttpToCGISocketFactory();

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return defaultFactory.createSocket( host, port );
        /*
        // First, try to do a direct connect
        try {
            
        } catch ( Exception e ) {
            log.log( Level.FINE, "Failed default socket connect to " + host + ":" + port + ", falling back to CGI", e );
            // Otherwise, fallback to CGI
            return cgiFactory.createSocket( host, port );
        }
         * 
         */
    }
}
