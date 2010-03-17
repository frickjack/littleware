/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

// pull in these services -- ugh
import javax.swing.SwingUtilities;
import littleware.asset.AssetManager;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.client.AssetManagerService;
import littleware.asset.client.AssetSearchService;
import littleware.asset.client.LittleService;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.PropertiesLoader;
import littleware.base.UUIDFactory;
import littleware.base.swing.JPasswordDialog;
import littleware.security.AccountManager;
import littleware.security.LittleUser;
import littleware.security.SecurityAssetType;
import littleware.security.client.AccountManagerService;

/**
 * Bind the implementation of each 
 *    ServiceType.getMembers()
 * interface class
 * to SessionHelper.getService( n_type );
 * This module may be initialized with the SessionHelper
 * from which to acquire service implementations,
 * otherwise configure executes a Swing-UI based authentication.
 * Also binds LittleSession to helper.getSession
 */
public class ClientServiceGuice implements LittleGuiceModule {

    private static final Logger log = Logger.getLogger(ClientServiceGuice.class.getName());
    private SessionHelper helper = null;
    private Maybe<CallbackHandler> maybeHandler = Maybe.empty();
    private Maybe<String> host = Maybe.empty();

    /**
     * Property specifies the server host to authenticate to
     * if authentication is necessary.
     */
    public Maybe<String> getHost() {
        return host;
    }

    public void setHost(Maybe<String> host) {
        this.host = host;
    }

    /**
     * Inject helper dependency
     * 
     * @param helper
     */
    public ClientServiceGuice(SessionHelper helper) {
        this.helper = helper;
    }

    /**
     * Parameterless constructor - client must inject
     * dependency by hand before configuring a GUICE injector,
     * otherwise Guice configure will first attempt to setup
     * a session with data from the last_session.properties file,
     * and finally will prompt the user for name/password
     * using the registered CallbackHandler.
     */
    public ClientServiceGuice() {
    }

    /**
     * Same as parameterless constructor, but with an override
     * of the server host to connect to.
     */
    public ClientServiceGuice(String sHost) {
        host = Maybe.something(sHost);
    }

    /**
     * Allow the user to inject a CallbackHandler, otherwise
     * defaults to littleware.base.JPasswordDialog
     */
    public ClientServiceGuice(CallbackHandler handler) {
        maybeHandler = Maybe.something(handler);
    }

    @Override
    public SessionHelper getSessionHelper() {
        return helper;
    }

    @Override
    public void setSessionHelper(SessionHelper helper) {
        this.helper = helper;
    }

    private static <T extends LittleService> Provider<T> bind(final Binder binder,
            final ServiceType<T> service, final SessionHelper helper) {
        Provider<T> provider = new Provider<T>() {

            @Override
            public T get() {
                try {
                    T result = helper.getService(service);
                    if (null == result) {
                        throw new AssertionFailedException("Failure to allocate service: " + service);
                    }
                    return result;
                } catch (Exception e) {
                    throw new littleware.base.FactoryException("service retrieval failure for service " + service, e);
                }
            }
        };
        binder.bind(service.getInterface()).toProvider(provider);
        log.log(Level.FINE, "Just bound service " + service + " interface " + service.getInterface().getName());
        return provider;
    }
    private static final String os_propfile = "latest_session.properties";
    private static final String os_name_key = "session.username";
    private static final String os_session_key = "session.id";

    /**
     * Retrieve a SessionHelper for the current environment using the
     * given CallbackHandler to prompt the user if necessary,
     * but first attempt to derive session info from latest_session.properties.
     * 
     * @param manager to authenticate against
     * @param handler to prompt user username/password
     * @param  number of retries to allow the user
     * @return authenticated SessionHelper 
     * @throws javax.security.auth.login.LoginException on credential failure
     * @throws java.io.IOException if user cancels out of login prompt
     */
    public static SessionHelper authenticate(SessionManager manager,
            CallbackHandler handler,
            int i_retry) throws IOException, LoginException {

        if (i_retry < 1) {
            i_retry = 1;
        }
        Properties prop_session = new Properties();
        try {
            prop_session = PropertiesLoader.get().loadProperties(os_propfile);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to load " + os_propfile + ", proceeding ...");
        }

        String s_name = prop_session.getProperty(os_name_key,
                System.getProperty("user.name", "username"));
        if (null == s_name) {
            s_name = "";
        } else {
            s_name = s_name.toLowerCase();
        }
        String s_session_id = prop_session.getProperty(os_session_key);

        if (s_session_id != null) {
            try {
                SessionHelper helper = manager.getSessionHelper(UUIDFactory.parseUUID(s_session_id));
                // Make sure the session hasn't expired by retrieving a service
                AssetRetriever search = helper.getService(ServiceType.ASSET_SEARCH);
                // ok
                return helper;
            } catch (Exception ex) {
                log.log(Level.FINE, "Failed to connect to session: " + s_session_id + ", continueing", ex);
            }
        }

        final Callback[] v_callback = {
            new NameCallback("Enter username", s_name),
            new PasswordCallback("Enter password", false),
            new TextOutputCallback(TextOutputCallback.INFORMATION, "Please login")
        };
        final Maybe<File> maybeHome = PropertiesLoader.get().getLittleHome();

        for (int i = 0; i < i_retry; ++i) {
            String s_password = "";
            if (0 != i) {
                // First time through just check if can login
                // as default user with null password
                try {
                    handler.handle(v_callback);
                    s_name = ((NameCallback) v_callback[0]).getName();
                    s_password = new String(((PasswordCallback) v_callback[1]).getPassword());
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (IOException ex) {
                    throw ex;
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to authenticate to " + SessionUtil.get().getRegistryHost(),
                            ex);
                    throw new FailedLoginException("Unable to authenticate: " + ex.getMessage());
                }
            }
            try {
                SessionHelper helper = manager.login(s_name, s_password, "client login");
                if ( maybeHome.isSet() ) {
                    try {
                        prop_session.setProperty(os_name_key, s_name);
                        prop_session.setProperty(os_session_key, helper.getSession().getId().toString());
                        PropertiesLoader.get().safelySave(prop_session,
                                new File(maybeHome.get(), os_propfile));
                    } catch (IOException ex) {
                        log.log(Level.FINE, "Failed to save session info", ex);
                    }
                }
                return helper;
                /*
                } catch (RuntimeException ex) {
                throw ex;
                 */
            } catch (Exception ex) {
                log.log(Level.FINE, "Failed login attempt " + i, ex);
            } finally {
                v_callback[2] = new TextOutputCallback(TextOutputCallback.ERROR, "Login Failed");
            }
        }
        throw new FailedLoginException("Retries expended");
    }

    /** Internal utility, sets ohelper */
    private void authenticate(CallbackHandler callback) {
        try {
            if (host.isSet()) {
                // connect to default server
                final int port = SessionUtil.get().getRegistryPort();
                log.log( Level.FINE, "Authenticating to " + host + "/" + port );
                helper = authenticate(SessionUtil.get().getSessionManager(host.get(), port), callback, 3);
            } else {
                helper = authenticate(SessionUtil.get().getSessionManager(), callback, 3);
            }
        } catch (Exception ex) {
            throw new AssertionFailedException("Failed to authenticate: " + host.getOr( SessionUtil.get().getRegistryHost() ), ex);
        }

    }

    /**
     * Authenticate with the registered CallbackHandler if the SessionHelper
     * is not already injected, then setup the client bindings to the
     * littleware services.
     * 
     * @param binder
     */
    @Override
    public void configure(Binder binder) {
        if (null == helper) {
            if (maybeHandler.isSet()) {
                // NOTE: avoid swing calls in server environment!
                authenticate(maybeHandler.get());
            } else if (SwingUtilities.isEventDispatchThread()) {
                authenticate(new JPasswordDialog("", ""));
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            authenticate(new JPasswordDialog("", ""));
                        }
                    });

                } catch (Exception ex) {
                    throw new AssertionFailedException("Failed to init dialog callback handler", ex);
                }
            }
            if (null == helper) {
                throw new AssertionFailedException("Failed authentication");
            }
        }

        for (ServiceType<? extends LittleService> service : ServiceType.getMembers()) {
            log.log(Level.FINE, "Binding service provider: " + service);
            bind(binder, service, helper);
        }

        // Frick - need to bind core interfaces here explicitly
        binder.bind(AssetSearchManager.class).to(AssetSearchService.class);
        binder.bind(AccountManager.class).to(AccountManagerService.class);
        binder.bind(AssetManager.class).to(AssetManagerService.class);
        binder.bind(SessionHelper.class).toInstance(helper);
        binder.bind(LittleSession.class).toProvider(new Provider<LittleSession>() {

            @Override
            public LittleSession get() {
                try {
                    return helper.getSession();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new AssertionFailedException("Failed to retrieve active session", e);
                }
            }
        });
        try {
            binder.bindConstant().annotatedWith(Names.named("littleware.startupServerVersion")).to(helper.getServerVersion());
        } catch (RemoteException ex) {
            throw new AssertionFailedException( "Failed to bind littleware.startupServerVersion constant", ex );
        }

        binder.bind(LittleUser.class).toProvider(new Provider<LittleUser>() {

            @Override
            public LittleUser get() {
                try {
                    final AssetSearchManager search = helper.getService(ServiceType.ASSET_SEARCH);
                    return search.getAsset(helper.getSession().getOwnerId()).get().narrow(LittleUser.class);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new AssertionFailedException("Failed to retrieve active session", e);
                }
            }
        });
        binder.bind(AssetRetriever.class).to(AssetSearchManager.class);

        log.log(Level.FINE, "Forcing load of SecurityAssetType and AssetType: " +
                AssetType.HOME + ", " + SecurityAssetType.USER);
    }
}
