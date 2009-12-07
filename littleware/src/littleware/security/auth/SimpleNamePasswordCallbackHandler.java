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

/**
 * Simple callback handler handles NameCallback and PasswordCallback
 * instances by simply supplying the name and passoword passed to the
 * handler's constructor.
 */
public class SimpleNamePasswordCallbackHandler implements CallbackHandler {

    private String os_name;
    private String os_password;

    /**
     * Constructor stashes the name and password to pass to callbacks 
     * during callback handling
     *
     * @param s_name of user to authenticate
     * @param s_password of user
     */
    public SimpleNamePasswordCallbackHandler(String s_name, String s_password) {
        os_name = s_name;
        os_password = s_password;
    }

    /**
     * Handle the given array of callbacks
     *
     * @param v_callbacks to manage
     * @exception UnsupportedCallbackException if recieve other than Name or Password callback
     * @exception IOException to complete interface
     */
    @Override
    public void handle(Callback[] v_callbacks) throws UnsupportedCallbackException, IOException {
        for (int i = 0; i < v_callbacks.length; ++i) {
            if (v_callbacks[i] instanceof NameCallback) {
                ((NameCallback) v_callbacks[i]).setName(os_name);
            } else if (v_callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback) v_callbacks[i]).setPassword(os_password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(v_callbacks[i], "Unsupported callback");
            }
        }
    }
}
