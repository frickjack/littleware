package littleware.security.auth;

import com.google.inject.Inject;
import java.rmi.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.net.*;
import littleware.base.*;

/**
 * Just a little utility class that provides
 * a standard access to a SessionManager.
 * Once a client has a SessionManager, the client
 * can login, and access the other remote managers.
 */
public class SessionUtil {

    private static final Logger olog = Logger.getLogger(SessionUtil.class.getName());
    public static final int MAX_REMOTE_RETRY = 3;

    private  int oi_registry_port = 1239;
    private  String os_registry_host = "localhost";

    {
        try {
            os_registry_host = InetAddress.getLocalHost().getHostName();
        } catch (java.io.IOException e) {
            olog.log(Level.SEVERE, "Unable to cache hostname, caught: " + e);
        }

        try {
            Properties prop_user = PropertiesLoader.get().loadProperties();

            String s_port_override = prop_user.getProperty("int.lw.rmi_port");

            if (null != s_port_override) {
                try {
                    oi_registry_port = Integer.parseInt(s_port_override);
                } catch (NumberFormatException e) {
                    olog.log(Level.INFO, "Failure parsing int.lw.rmi_port system property, caught: " + e);
                }
            }

            String s_host_override = prop_user.getProperty("littleware.rmi_host");

            if (null != s_host_override) {
                os_registry_host = s_host_override;
            }

        } catch (java.io.IOException e) {
            olog.log(Level.SEVERE, "Unable to read server properties, caught: " + e);
        }
    }

    
    private static SessionUtil osingle = null;
    
    public static SessionUtil get () {
        if ( osingle != null ) {
            return osingle;
        }
        synchronized (olog) {
            if ( osingle != null ) {
                return osingle;
            }
            osingle = new SessionUtil ();
            return osingle;
        }
    }
    

    /**
     * Get the default port on which a client expects the RMI registry to listen.
     * Set to int.lw.rmi_port system property if exists, otherwise 1239.
     */
    public int getRegistryPort() {
        return oi_registry_port;
    }

    /**
     * Get the default port on which we expect the RMI registry to listen.
     * Set to littleware.rmi_host system property if exists, otherwise "localhost".
     */
    public String getRegistryHost() {
        return os_registry_host;
    }

    @Inject
    private SessionManager om_local = null;

    /**
     * Do a registry lookup, and return the SessionManager.
     * If the SessionManager is remote - then return a proxy
     * that auto-retries on RemoteException up through the
     * SessionHelper.getService() remote interfaces.
     *
     * @param s_host to connect to
     * @param i_port to connect to.  If 0, then attempt to access
     *             server code local to this JVM rather than use RMI
     */
    public SessionManager getSessionManager(String s_host, int i_port) throws RemoteException, NotBoundException {
        if (i_port != 0) {
            RemoteExceptionHandler handler_retry = new RemoteExceptionHandler();

            while (true) {
                // Retry on RemoteException
                try {
                    SessionManager m_session = (SessionManager) Naming.lookup("//" + s_host + ":" + i_port + "/littleware/SessionManager");
                    return new SessionManagerProxy(m_session, new URL("http://" + s_host + ":" + i_port + "/littleware/SessionManager"));
                } catch (RemoteException e) {
                    handler_retry.handle(e);
                } catch (java.net.MalformedURLException e) {
                    throw new AssertionFailedException("Bad host/port specified, caught: " + e, e);
                }
            }
        } else if (null != om_local) { // check cache
            // Client and server are running in the same JVM -
            // so just use a normal object reference and bypass RMI.
            // om_local must be initialized externally.
            return om_local;
        } else {
            throw new IllegalStateException( "SessionUtil configured in local-server mode, but local SessionManager not injected" );
        }
    }

    /**
     * Get the SessionManager using the default host/port or
     * do not use RMI (access localhost/0) if isServerInJvm() is true.
     */
    public SessionManager getSessionManager() throws RemoteException, NotBoundException {
        if ( null != om_local ) {
            return getSessionManager("localhost", 0);
        } else {
            return getSessionManager(getRegistryHost(), getRegistryPort());
        }
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
