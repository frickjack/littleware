package littleware.security.server;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import java.security.acl.*;
import javax.security.auth.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.*;

/**
* RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiAccountManager extends UnicastRemoteObject implements AccountManager {
	private AccountManager       om_proxy = null;
	
	
	public RmiAccountManager ( AccountManager m_proxy ) throws RemoteException {
		super ();
		om_proxy = m_proxy;
	}
	
	
	public LittlePrincipal getPrincipal ( String s_name
													) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException  {
		return om_proxy.getPrincipal ( s_name );
	}



	public LittlePrincipal getPrincipal ( UUID u_id ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException
	{
		return om_proxy.getPrincipal ( u_id );
	}


	public int incrementQuotaCount () throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_proxy.incrementQuotaCount ();
	}


	public LittleUser createUser ( LittleUser p_user, 
							 String s_password 
							 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_proxy.createUser ( p_user, s_password );
	}


	public LittleUser updateUser ( LittleUser p_update, String s_password, 
							 String s_update_comment 
							 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_proxy.updateUser ( p_update, s_password, s_update_comment );
	}


	public boolean isValidPassword ( String s_password ) throws RemoteException
	{
		return om_proxy.isValidPassword ( s_password );
	}


	public LittleUser getAuthenticatedUser () throws GeneralSecurityException, RemoteException
	{
		return om_proxy.getAuthenticatedUser ();
	}


	public Quota getQuota ( LittleUser p_user ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_proxy.getQuota ( p_user );
	}

}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

