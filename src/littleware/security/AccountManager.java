package littleware.security;

import java.security.Principal;
import java.security.acl.*;
import java.util.*;
import javax.security.auth.Subject;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.rmi.Remote;

import littleware.base.*;
import littleware.asset.*;


/**
 * Interface to a central security manager for managing
 * littleware java.security.Principals.
 * A generic AccountManager implementation may not restrict
 * read-access to littleware Principal data, but should
 * enforce write-access restrictions.
 */
public interface AccountManager extends Remote {
	
	/** group-name of admin group */
	public static final String LITTLEWARE_ADMIN_GROUP = "group.littleware.administrator";
	/** Admin user name */
	public static final String LITTLEWARE_ADMIN = "littleware.administrator";
	/** group containing everybody */
	public static final String LITTLEWARE_EVERYBODY_GROUP = "group.littleware.everybody";
	/** Admin user id */
	public static final UUID UUID_ADMIN = littleware.base.UUIDFactory.parseUUID ( "00000000000000000000000000000000" );
	/** Admin group id */
	public static final UUID UUID_ADMIN_GROUP = littleware.base.UUIDFactory.parseUUID ( "89A1CB79B5944447BED9F38D398A7D12" );

	

	/**
	 * Retrieve the principal with the given name
	 *
	 * @param s_name to retrieve
	 * @return Principal might be a Group
	 * @exception NoSuchThingException if prinicipal with that name does not exist on this server
	 * @exception DataAccessException on failure to contact data store
	 * @exception AccessDeniedException if caller has insufficient privileges
	 * @exception ManagerException on other error condition
	 */
	public @ReadOnly LittlePrincipal getPrincipal ( String s_name
													) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;

	
	/**
	 * Retrieve the principal with the given id
	 *
	 * @param u_id to retrieve
	 * @return Principal might be a Group
	 * @exception NoSuchThingException if prinipal does not exist
	 * @exception DataAccessException on failure to contact data store
	 * @exception AccessDeniedException if caller has insufficient privileges
	 * @exception ManagerException on other error condition
	 */	
	public @ReadOnly LittlePrincipal getPrincipal ( UUID u_id ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	
	/**
	 * Increment the quota op-count on all the quotas
	 * in the quota-chain associated with the calling user.
	 * Internally does a setuid up to administrator to get permissions
	 * to update the underlying Quota assets.
	 *
	 * @return number of operations remaining in the user's quota, -1 indicates no limit
	 */
	int incrementQuotaCount () throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;

	
	/**
	 * Create a new user
	 *
	 * @param p_new asset with user data initialized - object-id gets reset 
	 * @param s_password must obey system password rules - ignored for Group type
	 * @param s_comment to attach to account
         * @return p_user with updates applied
	 * @exception IllegalNameException if name or password are not valid
	 * @exception DataAccessException on failure to contact data store
	 * @exception AccessDeniedException if caller has insufficient privileges
	 * @exception ManagerException on other error condition
	 * @exception NoSuchThingException if try to reference some invalid home asset or whatever
	 */
	public LittleUser createUser ( LittleUser p_user, 
							  String s_password 
							 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	
	
	/**
	 * Edit the given user asset
	 *
	 * @param p_update principal to edit - all Asset related data in this object is updated
	 * @param s_password to assign to the principal, "" to leave unchanged
	 * @param n_status to enable/disable user login privileges
	 * @param s_update_comment to assign, "" to leave unchanged
         * @return p_user with updates applied
	 * @exception NotOwnerException if caller does not have permission
	 * @exception DataAccessException on failure to contact data store
	 * @exception ManagerException on other error condition
	 * @exception IllegalNameException if invalid password given
	 */
	public LittleUser updateUser ( LittleUser p_update, String s_password, 
						     String s_update_comment 
							 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	
	/**
	 * Return true if this Manager will assign the given password to a Principal.
	 * Typical requirements are at least 9 characters including at least one letter and one number.
	 */
	public @ReadOnly boolean isValidPassword ( String s_password ) throws RemoteException;
	

	/** 
	 * Convenience function accesses the thread's access-control context
	 * and returns the currently authenticated LittleUser that the
	 * thread is executing as, or null if no authentication is current.
	 *
	 * @return authenticated user 
	 * @exception ManagerException on other error condition
	 */
	public @ReadOnly LittleUser getAuthenticatedUser () throws GeneralSecurityException, RemoteException;
	
	/**
	 * Get the end of the quota-chain associated with the given user.
	 *
	 * @return the user's quota, or null if quota of specified type is not 
	 *                              assigned to the user
	 * @exception NoSuchThingException if quota not found
	 */
	public Quota getQuota ( LittleUser p_user ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

