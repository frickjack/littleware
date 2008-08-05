/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Regulated by Software License
 */

package littleware.base;

import java.rmi.RemoteException;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;


/**
 * Specialization of UnicastRemoteObject that simplifies
 * experimentation with SocketFactory.
 */
public class LittleRemoteObject extends UnicastRemoteObject {
    private final static RMIServerSocketFactory ofactory_server = RMISocketFactory.getDefaultSocketFactory();
    private final static RMISocketFactory       ofactory = new sun.rmi.transport.proxy.RMIHttpToCGISocketFactory();
    
    public LittleRemoteObject () throws RemoteException {
        // disable for now ... super(0, ofactory, ofactory_server );
    }
}
