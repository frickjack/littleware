/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.auth.server;

import com.google.inject.Provider;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.security.auth.*;
import javax.security.auth.spi.*;
import javax.security.auth.login.*;
import javax.security.auth.callback.*;

// Source the SLIDE JAAS types
// disable for now import org.apache.slide.jaas.spi.*;

import littleware.asset.AssetSearchManager;
import littleware.asset.server.LittleTransaction;
import littleware.base.UUIDFactory;
import littleware.db.*;
import littleware.security.*;
import littleware.security.auth.server.db.DbAuthManager;

/**
 * Implementation of CallbackHandler authenticates given user and password
 * against Postgres user database.
 * Includes an &quot;authorized&quot; SimpleRole in the Subject&apos;s Principal set.
 */
public class SimpleDbLoginModule implements LoginModule {

    private static final Logger olog_generic = Logger.getLogger( SimpleDbLoginModule.class.getName() );
    private CallbackHandler ox_handler = null;
    private Subject oj_subject = null;

    private boolean ob_check_password = false;

    /**
     * Equivalent to SimpleDbLoginModule( false )
     */
    public SimpleDbLoginModule() {
    }

    /**
     * Initialize this login module so that it does not check the user password -
     * only verifies that the user exists in the littleware database.
     * Assumes some other required login module like LDAP verifies the user password.
     * Acquires the database connection pool via the SqlResourceBundle.
     *
     * @param b_check_password set true to check password against internal
     *                           littleware password database
     */
    public SimpleDbLoginModule(boolean b_check_password) {
        ob_check_password = b_check_password;
    }

    private static AssetSearchManager osearch = null;
    private static DbAuthManager om_dbauth = null;
    private static Provider<LittleTransaction>  oprovideTrans = null;

    /**
     * Inject the dependencies
     */
    public static void start( AssetSearchManager search, DbAuthManager m_dbauth, Provider<LittleTransaction> provideTrans ) {
        osearch = search;
        om_dbauth = m_dbauth;
        oprovideTrans = provideTrans;
    }

    
    /**
     * Initialize the module with data from underlying
     * login context
     *
     * @param j_subject to manage
     * @param x_handler to invoke for user-supplied data
     * @param v_shared_state map shared with other login modules
     * @param v_options login options
     */
    @Override
    public void initialize(Subject j_subject,
            CallbackHandler x_handler,
            Map v_shared_state,
            Map v_options) {
        oj_subject = j_subject;
        ox_handler = x_handler;
    }

    /**
     * Attempt phase-1 login using cached CallbackHandler to get user info
     *
     * @return true if authentication succeeds, false to ignore this module
     * @exception LoginException if authentication fails
     */
    @Override
    public boolean login() throws LoginException {
        if (null == ox_handler) {
            throw new LoginException("No CallbackHandler registered with module");
        }
        if (null == oj_subject) {
            throw new LoginException("Subject never setup");
        }
        if ( null == osearch ) {
            throw new LoginException( "AccountManager dependency never injected" );
        }
        if ( null == om_dbauth ) {
            throw new LoginException( "DbAuthManager dependency never injected" );
        }

        String s_user = null;
        String s_password = null;

        try {
            // Collect username and password via callbacks
            Callback[] v_callbacks = {
                new NameCallback("Enter username"),
                new PasswordCallback("Enter password", false)
            };
            ox_handler.handle(v_callbacks);

            s_user = new String(((NameCallback) v_callbacks[ 0]).getName());
            s_password = new String(((PasswordCallback) v_callbacks[ 1]).getPassword());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LoginException("Failure handling callbacks, caught: " + e);
        }

        // disable for now - no good way to inject TransactionManager ...
        final LittleTransaction trans_login = oprovideTrans.get();
        trans_login.startDbAccess();
        try {
            final LittleUser user = osearch.getByName(s_user, SecurityAssetType.USER ).get();
            // Ok, user exists - now verify password if necessary
            if (ob_check_password) {
                DbReader<Boolean, String> sql_check = om_dbauth.makeDbPasswordLoader(user.getObjectId());
                Boolean b_result =
                        sql_check.loadObject(s_password);

                if (b_result.equals(Boolean.FALSE)) {
                    olog_generic.log(Level.WARNING, "Invalid password for user: " +
                            s_user + " (" + UUIDFactory.makeCleanString(user.getObjectId()) +
                            ") -> " + s_password);
                    throw new LoginException();
                }
            }

            final String s_role = "authorized";

            oj_subject.getPrincipals().add(user);
            oj_subject.getPrincipals().add(new littleware.security.SimpleRole(s_role));
            //oj_subject.getPrincipals ().add ( new SlidePrincipal ( p_user.getName () ) );
            //oj_subject.getPrincipals ().add ( new SlideRole ( s_role ) );
            olog_generic.log(Level.FINE, "User authenticated: " + user.getName());

        } catch (RuntimeException e) {
            throw e;
        } catch (LoginException e) {
            throw e;
        } catch (Exception e) {
            olog_generic.log(Level.FINE, "Authenticateion of " + s_user + "failed, caught: " + e);
            throw new FailedLoginException("Authentication of " + s_user + " failed, caught: " + e);
        } finally {
            trans_login.endDbAccess();
        }

        return true;
    }

    /**
     * Phase 2 commit of login.
     * Idea is that multiple modules may go through a phase 1 login,
     * then phase 2 comes through once all is ok.
     *
     * @exception LoginException if commit fails
     */
    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    /**
     * Abort the login process - always returns true for now
     *
     * @exception LoginException if abort fails
     */
    @Override
    public boolean abort() {
        return true;
    }

    /**
     * Logout the subject associated with this module's context
     *
     * @return true if logout ok, false to ignore this module
     * @exception LoginException if logout fails
     */
    @Override
    public boolean logout() throws LoginException {
        return true;
    }
}

