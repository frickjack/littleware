/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.base;

import javax.security.auth.callback.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple callback handler handles NameCallback and PasswordCallback
 * instances by simply supplying the name and passoword passed to the
 * handler's constructor.
 */
public class LoginCallbackHandler implements CallbackHandler {
    private static final Logger log = Logger.getLogger( LoginCallbackHandler.class.getName() );

    private final String name;
    private final String password;

    /**
     * Constructor stashes the name and password to pass to callbacks 
     * during callback handling
     *
     * @param name of user to authenticate
     * @param password of user
     */
    public LoginCallbackHandler(String name, String password) {
        this.name = name;
        this.password = password;
    }

    /**
     * Handle the given array of callbacks
     *
     * @param callbacks to manage
     * @throws UnsupportedCallbackException if recieve other than Name or Password callback
     * @throws IOException to complete interface
     */
    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException, IOException {
        for (int i = 0; i < callbacks.length; ++i) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback) callbacks[i]).setName(name);
            } else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
            } else if ( callbacks[i] instanceof TextOutputCallback ) {
                log.log( Level.INFO, ((TextOutputCallback) callbacks[i]).getMessage() );
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unsupported callback: " + callbacks[i].toString() );
            }
        }
    }
}
