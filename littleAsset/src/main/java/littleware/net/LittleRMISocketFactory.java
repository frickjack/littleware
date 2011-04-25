/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Reserved - refer to the software license
 */

package littleware.net;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.io.Serializable;
import java.rmi.server.RMISocketFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom socket factory tries to facilitate RMI fallback to CGI gateway
 */
public class LittleRMISocketFactory implements Serializable, RMIClientSocketFactory {
    private static final Logger olog = Logger.getLogger( LittleRMISocketFactory.class.getName () );
    private final static RMIClientSocketFactory       ofactory_default =
            (null == RMISocketFactory.getSocketFactory()) ?
                RMISocketFactory.getDefaultSocketFactory()
                : RMISocketFactory.getSocketFactory ()
                ;
    private final static RMIClientSocketFactory       cgiFactory = new sun.rmi.transport.proxy.RMIHttpToCGISocketFactory();

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        // First, try to do a direct connect
        try {
            return ofactory_default.createSocket( host, port );
        } catch ( Exception e ) {
            olog.log( Level.FINE, "Failed default socket connect to " + host + ":" + port + ", falling back to CGI", e );
            // Otherwise, fallback to CGI
            return cgiFactory.createSocket( host, port );
        }
    }
}
