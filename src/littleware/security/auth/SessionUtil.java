package littleware.security.auth;

import java.rmi.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.lang.reflect.*;
import java.security.*;
import java.net.*;
import littleware.base.*;
import littleware.base.stat.*;
import littleware.base.BaseException;

/**
 * Just a little utility class that provides
 * a standard access to a SessionManager.
 * Once a client has a SessionManager, the client
 * can login, and access the other remote managers.
 */
public abstract class SessionUtil {

    private static final Logger olog_generic = Logger.getLogger(SessionUtil.class.getName());
    public static final int MAX_REMOTE_RETRY = 3;
    private static boolean ob_local_server = false;
    private static int oi_registry_port = 1239;
    private static String os_registry_host = "localhost";

    static {
        try {
            os_registry_host = InetAddress.getLocalHost().getHostName();
        } catch (java.io.IOException e) {
            olog_generic.log(Level.SEVERE, "Unable to cache hostname, caught: " + e);
        }

        try {
            Properties prop_user = PropertiesLoader.loadProperties("littleware.properties", new Properties());

            String s_port_override = prop_user.getProperty("littleware.rmi_port");

            if (null != s_port_override) {
                try {
                    oi_registry_port = Integer.parseInt(s_port_override);
                } catch (NumberFormatException e) {
                    olog_generic.log(Level.INFO, "Failure parsing littleware.rmi_port system property, caught: " + e);
                }
            }

            String s_host_override = prop_user.getProperty("littleware.rmi_host");

            if (null != s_host_override) {
                os_registry_host = s_host_override;
            }

            String s_server_type = prop_user.getProperty("littleware.rmi_server_type");

            if ((null != s_server_type) && s_server_type.equals("local")) {
                ob_local_server = true;
            }
        } catch (java.io.IOException e) {
            olog_generic.log(Level.SEVERE, "Unable to read server properties, caught: " + e);
        }
    }

    /**
     * Return true if this JVM has server code running within it,
     * so that client code may bypass RMI.
     * Return false if RMI or other RPC must be used to access the server.
     * The default getSessionManager() (below) will pass a 0 port number
     * through to getSessionManager( "bla", 0 ) to get a local object.
     * Toggle to true via the littleware.rmi_server_type property in
     * the littleware.properties file (defaults to false).
     */
    public static boolean isServerInJvm() {
        return ob_local_server;
    }

    /**
     * Get the default port on which a client expects the RMI registry to listen.
     * Set to littleware.rmi_port system property if exists, otherwise 1239.
     */
    public static int getRegistryPort() {
        return oi_registry_port;
    }

    /**
     * Get the default port on which we expect the RMI registry to listen.
     * Set to littleware.rmi_host system property if exists, otherwise "localhost".
     */
    public static String getRegistryHost() {
        return os_registry_host;
    }
    private static SessionManager om_local = null;

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
    public static SessionManager getSessionManager(String s_host, int i_port) throws RemoteException, NotBoundException {
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
        } else if (null != om_local) {
            return om_local;
        } else {
            // Client and server are running in the same JVM - so just use a normal object reference and bypass RMI
            try {
                PrivilegedExceptionAction<SessionManager> act_setup = new PrivilegedExceptionAction<SessionManager>() {

                    public SessionManager run() throws Exception {
                        ResourceBundle bundle_security = ResourceBundle.getBundle("littleware.security.server.SecurityResourceBundle");
                        SessionManager m_session = (SessionManager) bundle_security.getObject("SessionManager");
                        SubjectInvocationHandler<SessionManager> handler_session = new SubjectInvocationHandler<SessionManager>(null, m_session, olog_generic, new SimpleSampler());
                        return (SessionManager) java.lang.reflect.Proxy.newProxyInstance(SessionManager.class.getClassLoader(),
                                new Class[]{SessionManager.class},
                                handler_session);
                    }
                };

                om_local = AccessController.doPrivileged(act_setup);

                return om_local;
            } catch (PrivilegedActionException e) {
                olog_generic.log(Level.SEVERE, "Failed setup, caught: " + e + ", with cause: " + e.getException() + ", " + BaseException.getStackTrace(e));
                throw new AssertionFailedException("Failed to setup SessionManager, caught: " + e + ", with cause: " + e.getException(), e.getException());
            } catch (Throwable e) {
                olog_generic.log(Level.SEVERE, "Failed setup, caught: " + e + ", " + BaseException.getStackTrace(e));
                throw new AssertionFailedException("Failed SessionUtil setup, caught: " + e, e);
            }
        }
    }

    /**
     * Get the SessionManager using the default host/port or
     * do not use RMI (access localhost/0) if isServerInJvm() is true.
     */
    public static SessionManager getSessionManager() throws RemoteException, NotBoundException {
        if (isServerInJvm()) {
            return getSessionManager("localhost", 0);
        } else {
            return getSessionManager(getRegistryHost(), getRegistryPort());
        }
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
