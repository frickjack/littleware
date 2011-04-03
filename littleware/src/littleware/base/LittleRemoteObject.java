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

/**
 * Specialization of UnicastRemoteObject that simplifies
 * experimentation with SocketFactory.
 */
public class LittleRemoteObject extends UnicastRemoteObject {

    private static final Logger log = Logger.getLogger(LittleRemoteObject.class.getName());
    private static final int registryPort;

    static {
        int port = 1239;

        try {
            final Properties userProps = PropertiesLoader.get().loadProperties();

            final String portOverride = userProps.getProperty("int.lw.rmi_port");

            if (null != portOverride) {
                try {
                    port = Integer.parseInt(portOverride);
                } catch (NumberFormatException e) {
                    log.log(Level.INFO, "Failure parsing int.lw.rmi_port system property, caught: " + e, e);
                }
            }
        } catch (java.io.IOException ex ) {
            log.log(Level.SEVERE, "Unable to read server properties", ex );
        }
        registryPort = port;
    }

    public static int getRegistryPort() {
        return registryPort;
    }

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
