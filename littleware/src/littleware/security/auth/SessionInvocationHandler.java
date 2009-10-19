/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.auth;

import java.lang.reflect.*;
import java.util.logging.Logger;
import java.util.*;
import java.security.*;
import javax.security.auth.Subject;
import java.rmi.RemoteException;

import littleware.base.*;
import littleware.asset.AssetException;
import littleware.base.stat.Sampler;


/**
 * Specialization of SubjectInvocationHandler that pulls the Subject
 * out of a session asset, and periodically reloads the session to
 * verify ReadOnly and SessionExpires tests.
 *
 * @deprecated do not invalidate an active session -
 *             just re-authenticate at login time
 */
public class SessionInvocationHandler<T> extends SubjectInvocationHandler<T> {
	private SessionHelper     om_helper = null;
		
	/**
	 * Stash the caller and real implementation for use at invoke() time
	 *
	 * @param a_session to invoke methods as, and
	 *               periodically reload to check for session-expired
	 *              and read-only session limitations
	 * @param x_real object to call through to
	 * @param log_call to log method calls to including who and how long to run
	 * @param stat_call to report call runtime to
	 * @param m_retriever local AssetRetriever to reload session-data with
	 */
	public SessionInvocationHandler ( Subject j_caller, T x_real, 
									  Sampler stat_call, 
									  SessionHelper m_helper
									  ) {
		super ( j_caller, x_real, stat_call );
		om_helper = m_helper;
		if ( null == j_caller ) {
			throw new NullPointerException ( "Caller is null" );
		}
	}
	
	public Object	invoke( Object proxy, Method method_call, Object[] v_args) throws Throwable {			
		Date           t_now = new Date ();
		PrivilegedExceptionAction  act_getsession = new PrivilegedExceptionAction () {
			public LittleSession run () throws BaseException, GeneralSecurityException,
			AssetException, RemoteException {
				 return om_helper.getSession ();
			}
		};

                /*..
		LittleSession a_session = null;
		try {
			a_session = (LittleSession) AccessController.doPrivileged ( new MakePrivilegedAction( act_getsession ) );
			//a_session = (LittleSession) Subject.doAs ( getCaller (), act_getsession );
		} catch ( PrivilegedActionException e ) {
			throw e.getCause ();
		}
		
		if ( t_now.getTime () > a_session.getEndDate ().getTime () ) {
			throw new SessionExpiredException ( UUIDFactory.makeCleanString ( a_session.getObjectId () ) );
		}
		if ( a_session.isReadOnly () 
			 && (! method_call.isAnnotationPresent( ReadOnly.class ) )
			 ) {
			throw new ReadOnlyException ();
		}
                 */
		return super.invoke ( proxy, method_call, v_args );
	}
}

