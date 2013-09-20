/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.fishRunner;

import java.text.ParseException;
import javax.security.auth.login.Configuration;

/**
 * Little helper assembles a login configuration 
 * from a string specifying a list of login module classes
 * "sufficient" for authentication, and a list of 
 * (name,password) pairs for backdoor logins.  For example:
 *    littleware.security.auth.server.LittleLoginModule;
 *    littleware.apps.littleId.client.controller.JaasLoginModule;
 *    Pairs:(littleware.administrator,adm1nP@ssw0rd),(reuben@frickjack.com)
 */
public class LoginConfigFactory {
    public Configuration buildFromSepc( String spec ) throws ParseException {
        throw new UnsupportedOperationException( "not yet implemented" );
    }
}
