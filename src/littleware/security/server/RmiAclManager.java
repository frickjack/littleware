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
public class RmiAclManager extends UnicastRemoteObject implements AclManager {
	private AclManager       om_proxy = null;
	
	
	public RmiAclManager ( AclManager m_proxy ) throws RemoteException {
		super ();
		om_proxy = m_proxy;
	}
	
	


	public LittleAcl getAcl ( String s_name 
							  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_proxy.getAcl ( s_name );
	}

	public LittleAcl getAcl( UUID u_id ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_proxy.getAcl ( u_id );
	}



}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

