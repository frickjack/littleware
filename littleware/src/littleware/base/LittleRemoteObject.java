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


/**
 * Specialization of UnicastRemoteObject that simplifies
 * experimentation with SocketFactory.
 */
public class LittleRemoteObject extends UnicastRemoteObject {
    private final static RMIServerSocketFactory ofactory_server = 
            (null == RMISocketFactory.getSocketFactory()) ?
                RMISocketFactory.getDefaultSocketFactory()
                : RMISocketFactory.getSocketFactory ()
                ;
    private final static RMIClientSocketFactory ofactory = new LittleRMISocketFactory ();
    
    public LittleRemoteObject () throws RemoteException {
        super(0, ofactory, ofactory_server );
    }
}
