package littleware.security;

import java.security.*;
import java.security.acl.*;
import java.util.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

import littleware.base.*;
import littleware.asset.*;

/**
 * Manage littleware Acl's
 */
public interface AclManager extends Remote {
	public final static String ACL_EVERYBODY_READ = "acl.littleware.everybody.read";
	
		
	
	/**
	 * Retrieve the Acl with the given name
	 *
	 * @param s_name
	 * @return acl or null if none by name exist
	 * @exception NoSuchThingException if does not exist
	 * @exception DataAccessException on failure to contact data store
	 * @exception AccessDeniedException if caller has insufficient privileges
	 * @exception ManagerException on other error condition
	 */
	public @ReadOnly LittleAcl getAcl ( String s_name 
								  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Retrieve the Acl with the given id
     *
	 * @exception NoSuchThingException if does not exist     
	 */
	public @ReadOnly LittleAcl getAcl( UUID u_id ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

