/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.internal;

import littleware.security.auth.client.internal.SessionManagerProxy;
import com.google.inject.Inject;
import java.rmi.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.net.*;
import littleware.base.*;
import littleware.security.auth.SessionManager;

/**
 * Just a little utility class that provides
 * a standard access to a SessionManager.
 * Once a client has a SessionManager, the client
 * can login, and access the other remote managers.
 */
public class SessionUtil {
    private static final Logger log = Logger.getLogger(SessionUtil.class.getName());
    public static final int MAX_REMOTE_RETRY = 3;
    private final int oi_registry_port = LittleRemoteObject.getRegistryPort();;
    private final String os_registry_host;

    {
        String host = "localhost";

        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (java.io.IOException e) {
            log.log(Level.SEVERE, "Unable to cache hostname, caught: " + e, e);
        }

        try {
            final Properties prop_user = PropertiesLoader.get().loadProperties();

            final String hostOverride = prop_user.getProperty("littleware.rmi_host");
            if (null != hostOverride) {
                host = hostOverride;
            }
        } catch (java.io.IOException ex) {
            log.log(Level.SEVERE, "Unable to read server properties, caught: " + ex, ex);
        }
        os_registry_host = host;
    }
    private static SessionUtil osingle = null;

    public static SessionUtil get() {
        if (osingle != null) {
            return osingle;
        }
        synchronized (log) {
            if (osingle != null) {
                return osingle;
            }
            osingle = new SessionUtil();
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
     * Defaults to littleware.rmi_host system property if exists, otherwise "localhost".
     */
    public String getRegistryHost() {
        return os_registry_host;
    }
    private SessionManager om_local = null;

    /**
     * Allow an in-memory server to inject an in-memory SessionManager
     * to the SessionUtil for client access.
     */
    @Inject
    public void injectLocalManager(SessionManager m_session) {
        if (null != om_local) {
            throw new IllegalStateException("Local sessionManager already injected");
        }
        try {
            om_local = new SessionManagerProxy(m_session, new URL("http://localManager"));
        } catch (MalformedURLException ex) {
            throw new AssertionFailedException("URL should be ok", ex);
        }
    }

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
                    final SessionManager m_session = (SessionManager) Naming.lookup("//" + s_host + ":" + i_port + "/littleware/SessionManager");
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
            throw new IllegalStateException("SessionUtil configured in local-server mode, but local SessionManager not injected");
        }
    }

    /**
     * Get the SessionManager using the default host/port or
     * do not use RMI (access localhost/0) if isServerInJvm() is true.
     */
    public SessionManager getSessionManager() throws RemoteException, NotBoundException {
        if (null != om_local) {
            return om_local;
        } else {
            return getSessionManager(getRegistryHost(), getRegistryPort());
        }
    }
}
