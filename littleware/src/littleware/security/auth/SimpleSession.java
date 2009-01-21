/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth;

import java.rmi.RemoteException;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.security.Principal;
import java.security.GeneralSecurityException;
import javax.security.auth.*;

import littleware.security.*;
import littleware.base.*;
import littleware.asset.*;

/**
 * Simple implementation of LittleSession 
 * interface backed by a database entry.
 */
public class SimpleSession extends SimpleAsset implements LittleSession {	
	/** Do-nothing constructor for java.io.Serializable */
	public SimpleSession () {
		this.setAssetType ( SecurityAssetType.SESSION );
		Date t_now = new Date ();
		this.setStartDate ( t_now );
		this.setEndDate ( new Date ( t_now.getTime () + 60*60*24*1000 ) );
	}
	
	/**
	 * Basic initializer just sets the principal name
	 *
	 * @param s_name must be alpha-numeric
	 * @param u_id littleware id number
	 * @param s_comment attached to user
	 */
	public SimpleSession ( String s_name, UUID u_id, String s_comment ) {
		this.setName ( s_name );
		this.setComment ( s_comment );
		this.setObjectId ( u_id );
		this.setAssetType ( SecurityAssetType.SESSION );
		Date t_now = new Date ();
		this.setStartDate ( t_now );
		this.setEndDate ( new Date ( t_now.getTime () + 60*60*24*1000 ) );
	}
	
	
	
	/** Just return the name */
    @Override
	public String toString () {
		return getName ();
	}
	
	public boolean isReadOnly () {
		return (getValue () != 0);
	}
	
	public void makeReadOnly () { setValue ( 1 ); }
	

	public void makeReadWrite () { setValue ( 0 ); }
	
	private Subject oj_cache = null;
	
	public Subject getSubject ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == oj_cache ) {
			LittleUser a_user = getCreator ( m_retriever );
			Set<Principal>  v_principals = new HashSet<Principal> ();
			v_principals.add ( a_user );
			oj_cache = new Subject ( true, v_principals, new HashSet<Object> (), new HashSet<Object> () );
		}
		return oj_cache;
	}
	
}

