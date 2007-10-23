package littleware.security.auth.server;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;
import javax.sql.DataSource;
import java.security.*;
import javax.security.auth.*;
import javax.security.auth.spi.*;
import javax.security.auth.login.*;
import javax.security.auth.callback.*;

// Source the SLIDE JAAS types
// disable for now import org.apache.slide.jaas.spi.*;

import littleware.asset.server.AssetResourceBundle;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.TransactionManager;
import littleware.base.UUIDFactory;
import littleware.db.*;
import littleware.security.*;
import littleware.security.server.SecurityResourceBundle;
import littleware.security.auth.*;
import littleware.security.auth.server.db.DbAuthManager;

/**
 * Implementation of CallbackHandler authenticates given user and password
 * against Postgres user database.
 * Includes an &quot;authorized&quot; SimpleRole in the Subject&apos;s Principal set.
 */
public class SimpleDbLoginModule implements LoginModule {
	private static Logger     olog_generic = Logger.getLogger ( "littleware.security.auth.server.SimpleDbLoginModule" );

	private CallbackHandler   ox_handler = null;
	private Subject           oj_subject = null;
	private AccountManager    om_account = null;
	private DbAuthManager     om_dbauth = null;
	private boolean           ob_check_password = false;
	
	/**
	 * Equivalent to SimpleDbLoginModule( false )
	 */
	public SimpleDbLoginModule () {
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
	public SimpleDbLoginModule ( boolean b_check_password ) {
		ob_check_password = b_check_password;
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
	public void initialize ( Subject j_subject,
							 CallbackHandler x_handler,
							 Map v_shared_state,
							 Map v_options
							 )
	{
		oj_subject = j_subject;
		ox_handler = x_handler;
		om_account = SecurityResourceBundle.getAccountManager ();

		{
			PrivilegedAction act_get_resource = new GetGuardedResourceAction ( AssetResourceBundle.getBundle (),
																			   AssetResourceBundle.Content.AuthDbManager.toString ()
																			   );
			om_dbauth = (DbAuthManager) AccessController.doPrivileged( act_get_resource );
		} 
	}
	
	/**
	 * Attempt phase-1 login using cached CallbackHandler to get user info
	 *
	 * @return true if authentication succeeds, false to ignore this module
	 * @exception LoginException if authentication fails
	 */
	public boolean login () throws LoginException {
		if ( null == ox_handler ) {
			throw new LoginException ( "No CallbackHandler registered with module" );
		}
		if ( null == oj_subject ) {
			throw new LoginException ( "Subject never setup" );
		}
		
		String s_user = null;
		String s_password = null;
		
		try {
			// Collect username and password via callbacks
			Callback[] v_callbacks = {
				new NameCallback ( "Enter username" ),
				new PasswordCallback ( "Enter password", false )
			};
			ox_handler.handle ( v_callbacks );
			
			s_user = new String ( ((NameCallback) v_callbacks[ 0 ]).getName () );
			s_password = new String ( ((PasswordCallback) v_callbacks[ 1 ]).getPassword () );
        } catch ( RuntimeException e ) {
            throw e;
		} catch ( Exception e ) {
			throw new LoginException ( "Failure handling callbacks, caught: " + e );
		}
	
		LittleTransaction  trans_login = TransactionManager.getTheThreadTransaction ();
        trans_login.startDbAccess ();
		try {	
			LittleUser  p_user = (LittleUser) om_account.getPrincipal ( s_user );
			// Ok, user exists - now verify password if necessary
			if ( ob_check_password ) {
				DbReader<Boolean,String> sql_check = om_dbauth.makeDbPasswordLoader ( p_user.getObjectId () );
				Boolean                  b_result =
					sql_check.loadObject ( s_password );
				
				if ( b_result.equals ( Boolean.FALSE ) ) {
					olog_generic.log ( Level.WARNING, "Invalid password for user: " +
									   s_user + " (" + UUIDFactory.makeCleanString ( p_user.getObjectId () ) + 
                                       ") -> " + s_password
									   );
					throw new LoginException ();
				}
			}
			
            final String    s_role = "authorized";
            
			oj_subject.getPrincipals ().add ( p_user );
            oj_subject.getPrincipals ().add ( new littleware.security.SimpleRole( s_role ) );
            //oj_subject.getPrincipals ().add ( new SlidePrincipal ( p_user.getName () ) );
            //oj_subject.getPrincipals ().add ( new SlideRole ( s_role ) );
            olog_generic.log ( Level.FINE, "User authenticated: " + p_user.getName () );
		
        } catch ( RuntimeException e ) {
            throw e;
		} catch ( LoginException e ) {
			throw e;
		} catch ( Exception e ) {
            olog_generic.log ( Level.FINE, "Authenticateion of " + s_user + "failed, caught: " + e );
			throw new FailedLoginException ( "Authentication of " + s_user + " failed, caught: " + e );
		} finally {
            trans_login.endDbAccess ();
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
	public boolean commit () throws LoginException {
		return true;
	}
	
	/**
	 * Abort the login process - always returns true for now
	 *
	 * @exception LoginException if abort fails
	 */
	public boolean abort () { return true; }
	
	/**
	 * Logout the subject associated with this module's context
	 *
	 * @return true if logout ok, false to ignore this module
	 * @exception LoginException if logout fails
	 */
	public boolean logout () throws LoginException {
		return true;
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

