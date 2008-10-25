/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Reserved - refer to the software license
 */

package littleware.base;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.io.Serializable;
import java.rmi.server.RMISocketFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pasquini
 */
public class LittleRMISocketFactory implements Serializable, RMIClientSocketFactory {
    private static final Logger olog = Logger.getLogger( LittleRMISocketFactory.class.getName () );
    private final static RMIClientSocketFactory       ofactory_default =
            (null == RMISocketFactory.getSocketFactory()) ?
                RMISocketFactory.getDefaultSocketFactory()
                : RMISocketFactory.getSocketFactory ()
                ;
    private final static RMIClientSocketFactory       ofactory_cgi = new sun.rmi.transport.proxy.RMIHttpToCGISocketFactory();

    public Socket createSocket(String s_host, int i_port) throws IOException {
        // First, try to do a direct connect
        try {
            return ofactory_default.createSocket( s_host, i_port );
        } catch ( Exception e ) {
            olog.log( Level.FINE, "Failed default socket connect to " + s_host + ":" + i_port + ", falling back to CGI", e );
            // Otherwise, fallback to CGI
            return ofactory_cgi.createSocket( s_host, i_port );
        }
    }
}
