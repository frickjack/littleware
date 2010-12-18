package littleware.base;

import java.lang.reflect.*;
import java.security.PrivilegedAction;
import javax.security.auth.login.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.stat.*;

/**
 * TODO - this thing is NOT FLESHED OUT YET.
 *
 * Convenience class for implementing interface
 * proxies that spool method calls off to
 * a command-spool.  Also takes care of collecting
 * call-time/wait-time histograms, and setting up the 
 * security activation context for the given LoginContext.
 *
 * Works like this: <br />
 *   <ul> <li> Client gets reference to Proxy with this InvocationHandler registered </li>
 *       <li> The invoke() spools a PriviledgedAction off to a spool 
 *               with the current LoginContext, and waits </li>
 *       <li> The spool services the privileged action which stashes its execution result </li>
 *       <li> The original caller wakes up and collects the result </li> 
 *   </ul>
 *
 * Would be nice if the original caller could go away, and come back later too.
 */
public class SpoolInvocationHandler<T> implements InvocationHandler {
	public static enum State { PENDING, RESULT_READY, EXCEPTION_READY, RUNTIME_FAILURE };

	/**
	 * Command object to send off to spool
	 */
	private class SpoolCommand implements java.security.PrivilegedAction<Object> {
		private Method    ox_method = null;
		private Object[]  ov_args = null;
		private Object    ox_result = null;
		private State     on_state = State.PENDING;
		
		
		public SpoolCommand ( Method x_method, Object[] v_args ) {
			ox_method = x_method;
			ov_args = v_args;
		}
		
		public synchronized void waitForResult () {
		}
		
		public State getState () { return on_state; }
		
		/** 
		 * PriveledgedAction implementation
		 */
		public synchronized Object run () {
			try {
				/*..
				ox_login_context.getSubject ().doPrivileged ( this );
				ox_method.invoke ();
				*/
			} catch ( Exception e ) {
			} finally {
				if ( on_state.equals ( State.PENDING ) ) {
					on_state = State.RUNTIME_FAILURE;
				}
				notifyAll ();
			}
			return null;
		}
		
	}
	
	private    T              ox_proxy_target = null;
	private    LoginContext   ox_login_context = null;
	private    Sampler        ox_sampler = new SimpleSampler ();
	private    Method         ox_method = null;
	private    Object[]       ov_args = null;
	//private    Spool          ox_spool = null;
	
	
	/**
	 * Constructor stashes data needed for the proxy implementation.
	 *
	 * @param x_proxy_target that implements the actual method -
	 *             invocations get forwarded to this implementation
	 * @param x_login_context to run methods against
	 */
	public SpoolInvocationHandler ( T x_proxy_target, LoginContext x_login_context ) {
		ox_proxy_target = x_proxy_target;
		ox_login_context = x_login_context;
	}
	
	/**
	 * Spool the call off to a spool, and wait for the result.
	 */
	public Object invoke ( Object x_proxy, Method x_method, Object[] v_args ) throws Throwable {
		return null;
	}

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

