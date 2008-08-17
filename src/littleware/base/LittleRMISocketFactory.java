/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Reserved - refer to the software license
 */

package littleware.base;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.io.Serializable;

/**
 *
 * @author pasquini
 */
public class LittleRMISocketFactory implements Serializable, RMIClientSocketFactory {
    private final static RMIClientSocketFactory       ofactory = new sun.rmi.transport.proxy.RMIHttpToCGISocketFactory();

    public Socket createSocket(String host, int port) throws IOException {
        return ofactory.createSocket( host, port );
    }
}
