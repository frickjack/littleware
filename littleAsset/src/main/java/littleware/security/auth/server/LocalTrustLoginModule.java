/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Map;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import littleware.base.Whatever;

/**
 * Allow other code running in the same process to register one-use credentials that
 * this LoginModule tests at login time.  For example - an in-process OpenId authentication
 * setup in a webapp can put a secret into session-scope and register the secret with
 * LocalTrustLoginModule.addSecret - that another piece of code (like an access-filter) 
 * can then use to setup a Littleware session.
 * 
 * This module takes one configuration option: userMustExist defaults to false, but may be set true
 */
public class LocalTrustLoginModule implements LoginModule {
    private static final Logger log = Logger.getLogger( LocalTrustLoginModule.class.getName() );
    
    private static Cache<String,String> secretsCache = CacheBuilder.newBuilder().softValues().build();
    
    /**
     * Register a secret that will be tested against the supplied password the next
     * time the user tries to authenticate.
     */
    public static void addSecret( String userName, String secret ) {
        secretsCache.put(userName, secret);
    }
    
    private Subject subject = null;
    private CallbackHandler callbackHandler = null;
    private boolean    userMustExist = true;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        final String mustExistStr = (String) options.get( "userMustExist" );
        userMustExist = !(
                (null != mustExistStr) && 
                (mustExistStr.equalsIgnoreCase( "false" ) || mustExistStr.equalsIgnoreCase( "no" ) )
                );
    }

    @Override
    public boolean login() throws LoginException {
        if (null == callbackHandler) {
            throw new LoginException("No CallbackHandler registered with module");
        }
        if (null == subject) {
            throw new LoginException("Subject never setup");
        }

        final String userName;
        final String password;

        try {
            // Collect username and password via callbacks
            final Callback[] callbacks = {
                new NameCallback("Enter username", "nobody"),
                new PasswordCallback("Enter password", false)
            };
            callbackHandler.handle(callbacks);

            userName = ((NameCallback) callbacks[ 0]).getName();
            password = new String(((PasswordCallback) callbacks[ 1]).getPassword());

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw (LoginException) (new LoginException("Failure handling callbacks")).initCause(ex);
        }

        if ( Whatever.get().empty( password ) || Whatever.get().empty( userName ) || userName.equals( "nobody" ) ) {
            throw new FailedLoginException( "Invalid user name" );
        }
        
        final String secret = secretsCache.getIfPresent( userName );
        secretsCache.invalidate( secret );
        if( (null != secret) && secret.equals( password ) &&
                ((! userMustExist) || LittleLoginModule.lookupUser(userName).isSet())
                ) {
            return true;
        }
        throw new FailedLoginException();
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }
}

