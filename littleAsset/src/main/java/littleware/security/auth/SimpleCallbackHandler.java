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

import javax.security.auth.callback.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple callback handler handles NameCallback and PasswordCallback
 * instances by simply supplying the name and passoword passed to the
 * handler's constructor.
 */
public class SimpleCallbackHandler implements CallbackHandler {
    private static final Logger log = Logger.getLogger( SimpleCallbackHandler.class.getName() );

    private String name;
    private String password;

    /**
     * Constructor stashes the name and password to pass to callbacks 
     * during callback handling
     *
     * @param name of user to authenticate
     * @param password of user
     */
    public SimpleCallbackHandler(String name, String password) {
        this.name = name;
        this.password = password;
    }

    /**
     * Handle the given array of callbacks
     *
     * @param callbacks to manage
     * @exception UnsupportedCallbackException if recieve other than Name or Password callback
     * @exception IOException to complete interface
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
