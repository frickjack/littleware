/*
 * Copyright (c) 2007,2008 Reuben Pasquini (catdogboy@yahoo.com)
 * All Rights Reserved
 */

package littleware.web.beans;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.BaseException;
import littleware.base.NoSuchThingException;
import littleware.base.AlreadyExistsException;

        
/**
 * Handy custom enum for managing String results
 * of form action methods.  A form action method may
 * return a String for easy handling by the JSF
 * processing engine - the ActionResult lets us avoid
 * typos and stay a bit type safe.
 * 
 * TODO: i18n support
 */
public class ActionResult {
    private static final Logger  olog_generic = Logger.getLogger ( ActionResult.class.getName () );
    
    private final String     os_id;
    private final String     os_message;
    private final Exception  oe_result;
    
    /**
     * Constructor injects ActionResult defs
     * 
     * @param s_id identifies the result type - ex: "Ok"
     * @param s_message to supplement the result with - 
     *                ex: "insufficient permissions"
     * @param e_result exception associated with result - may be null
     */
    public ActionResult ( String s_id, String s_message,
            Exception e_result ) {
        os_id = s_id;
        os_message = s_message;
        oe_result = e_result;
    }
    
    /**
     * Equivalent to ActionResult( result_in.getId (),
     *                             s_message, null );
     */
    public ActionResult ( ActionResult result_in, String s_message ) {
        this( result_in.getId (), s_message, null );
    }
    
    /**
     * Equivalent to ActionResult( result_in.getId (), s_message, e_result )
     */
    public ActionResult ( ActionResult result_in, String s_message,
            Exception e_result ) {
        this( result_in.getId (), s_message, e_result );
    }
    

    public String getId () { return os_id; }
    public String getMessage () { return os_message; }
    public Exception getException () { return oe_result; }
    
    /** Just return getId () */
    @Override
    public String toString () { return getId (); }
    
    /** Test getId() equality */
    @Override
    public boolean equals ( Object x_other )
    {
        return (
                (null != x_other)
                && (x_other instanceof ActionResult)
                && ((ActionResult) x_other).getId ().equals( getId () )
                );
    }
    
    /** Just hash on id */
    @Override
    public int hashCode () {
        return getId ().hashCode ();
    }
    
    /** Ok result */
    public static ActionResult  Ok = new ActionResult( "Ok", "action ran ok", null );
    /** OkNoEmail result */
    public static ActionResult  OkNoEmail = new ActionResult( "OkNoEmail", "action ran ok, but e-mail failed", null );
    /** Action failed due to bad user data or something like that */
    public static ActionResult  Failed = new ActionResult( "Failed", "action failed on inputs", null );
    /** Action failed due to internal system error or bug */
    public static ActionResult  Error = new ActionResult( "Error", "system error", null );
    /** Action not attempted, because user not in admin group */
    public static ActionResult  MustBeAdmin = new ActionResult( "MustBeAdmin", "Action requires administrator privileges", null );
    public static ActionResult  MustLogin = new ActionResult( "MustLogin", "Action requires login as non-guest user", null );
    public final static ActionResult  BadName = 
            new ActionResult( "BadName", "Name not long enough or something", null );    
    public final static ActionResult  NameUsed = 
            new ActionResult( "NameUsed", "Specified name already used", null );
    public final static ActionResult BadPassword = 
            new ActionResult( "BadPassword", "Password not strong enough", null );
    public final static ActionResult PasswordMismatch =
            new ActionResult ( "PasswordMismatch", "Password confirmation does not match password", null );
    public final static ActionResult NotFound =
            new ActionResult ( "NotFound", "Could not find requested thing", null );
        
    /**
     * Little utility generates standard ActionResult
     * in response to the given exception.  Also logs the exception.
     *
     * @throws e_action exception caught by a Form action that
     *      must now generate an appropriate ActionResult.
     */
    public static ActionResult handleException ( Exception e_action ) {
        olog_generic.log ( Level.INFO, "Caught exception", e_action );
        try {
            throw e_action;
        } catch ( RuntimeException e ) {
            return new ActionResult ( ActionResult.Error,
                    "RuntimeException", e
                    );
        } catch ( GeneralSecurityException e ) {
            return new ActionResult ( ActionResult.MustBeAdmin,
                    "GeneralSecurityException", e
                    );
        } catch ( RemoteException e ) {
            return new ActionResult ( ActionResult.Error,
                    "RemoteException", e
                    );
        } catch ( NoSuchThingException e ) {
            return new ActionResult ( ActionResult.Failed,
                    "attmept to access non-existent data", e
                    );
        } catch ( AlreadyExistsException e ) {
            return new ActionResult ( ActionResult.Failed,
                    "attempt to create something that already exists", e 
                    );
        } catch ( BaseException e ) {
            return new ActionResult ( ActionResult.Failed,
                    "littleware BaseException", e 
                    );
        } catch ( Exception e ) {
            return new ActionResult ( ActionResult.Error,
                    "Unexpected failure", e
                    );
        }
    }
}
