/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Regulated by Software License
 */
package littleware.base;

import java.rmi.RemoteException;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.security.auth.SessionUtil;

/**
 * Specialization of UnicastRemoteObject that simplifies
 * experimentation with SocketFactory.
 */
public class LittleRemoteObject extends UnicastRemoteObject {

    private static final Logger log = Logger.getLogger(LittleRemoteObject.class.getName());
    private static final int registryPort = SessionUtil.get().getRegistryPort();

    private final static RMIServerSocketFactory serverSocketFactory =
            (null == RMISocketFactory.getSocketFactory())
            ? RMISocketFactory.getDefaultSocketFactory()
            : RMISocketFactory.getSocketFactory();
    private final static RMIClientSocketFactory clientSocketFactory = new LittleRMISocketFactory();

    public LittleRemoteObject() throws RemoteException {
        //super( registryPort, clientSocketFactory, serverSocketFactory);
        super( registryPort );
    }
}
