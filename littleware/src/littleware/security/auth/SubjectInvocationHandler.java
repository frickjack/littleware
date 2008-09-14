package littleware.security.auth;

import java.lang.reflect.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import javax.security.auth.Subject;

import littleware.base.Timer;
import littleware.base.AssertionFailedException;
import littleware.base.stat.Sampler;
import littleware.base.AccessPermission;


/**
 * InvocationHandler to help support dynamic proxies that invoke methods
 * against a wrapped interface implementation as a given Subject  
 * through a Subject.doAs().  Logs the calling profile
 * to the supplied logger and stats-collector.
 * Also does an AccessController.doAs() to pull us up
 * into the littleware- execution context.
 */
public class SubjectInvocationHandler<T> implements InvocationHandler {
	private static Logger  olog_generic = Logger.getLogger ( "littleware.security.auth.SubjectInvocationHandler" );
	private Logger         olog_call = null;
	private Subject        oj_caller = null;
	private T              ox_real = null;
	private Sampler        ostat_call = null;
	
	/**
	 * Need a CommandObject for Subject.doAs.
	 * Separate object for each call to keep thread safe.
	 */
	private class RunAction implements PrivilegedExceptionAction<Object> {
		private Object[]     ov_args = null;
		private Method       omethod_call = null;
		
		public RunAction ( Method method_call, Object[] v_args ) {
			ov_args = v_args;
			omethod_call = method_call;
		}
		
		/**
		 * PrivilegedExceptionAction.run for Subject.doAs
		 */
		public Object run () throws Exception {
			try {
				return omethod_call.invoke ( ox_real, ov_args );
			} catch ( IllegalAccessException e ) {
				olog_generic.log ( Level.INFO, "Caught unexpected: " + e );
				throw new AssertionFailedException ( "Illegal access: " + e, e );
			} catch ( InvocationTargetException e ) {
				olog_generic.log ( Level.FINE, "FRICK: " + e );
				Throwable err = e.getCause ();
				
				if ( err instanceof Exception ) {
					throw (Exception) err;
				} else if ( err instanceof Error ) {
					throw (Error) err;
				} else {
					throw new AssertionFailedException ( "Unexpected throwable error: " + e, e );
				}
			}
		}
	}
	
	/**
	 * Another privileged action for AccessController.doAs().
	 * So the chain of events is:
	 *     client-> calls proxy
	 *              proxy -> invocation handler
	 *                   handler -> AccessController.doPrivileged
	 *                          priv_action -> Subject.doAs
	 *                                 SubjectHandler -> invoke reflection method
	 */
	public class MakePrivilegedAction implements PrivilegedExceptionAction<Object> {
		private PrivilegedExceptionAction<Object> oact_subject = null;
		
		/**
	     * Constructor stashes the action to pass on to the caller Subject
		 */
		public MakePrivilegedAction ( PrivilegedExceptionAction<Object> act_subject ) {
			oact_subject = act_subject;
		}
		
		/**
		 * Call through to the Subject so that we are 
		 * executing in a privileged execution context: 
		 *         oj_caller.doAs ( act_subject )
		 */
		public Object run () throws Exception {
			try {
				return Subject.doAs ( oj_caller, oact_subject );
			} catch ( PrivilegedActionException e ) {
				throw e.getException ();
			} 
		}
	}
	
	/**
	 * Stash the active Subject caller and real implementation for use at invoke() time
	 *
	 * @param j_caller to doAs() - pulled from AccessControlContext if null
	 * @param x_real object to call through to
	 * @param log_call to log method calls to including who and how long to run
	 * @param stat_call to report call runtime to
	 */
	public SubjectInvocationHandler ( Subject j_caller, T x_real, Logger log_call, Sampler stat_call ) {
		oj_caller = j_caller;
		if ( null == oj_caller ) {
			oj_caller = Subject.getSubject ( AccessController.getContext () );
		}
		ox_real = x_real;
		olog_call = log_call;
		ostat_call = stat_call;
		
		// Make sure the calling code has permission to setup a privileged proxy!
		Permission perm_proxy = new AccessPermission ( "privileged_proxy" );
		AccessController.checkPermission ( perm_proxy );
	}
	
	/**
	 * Little hook for subclasses to figure out who the caller is
	 */
	protected Subject getCaller () {
		return oj_caller;
	}
	
	public Object	invoke( Object proxy, Method method_call, Object[] v_args) throws Throwable {			
		littleware.base.Timer  timer_run = new littleware.base.Timer ();
		
		PrivilegedExceptionAction<Object> act_run = new RunAction ( method_call, v_args );
		/*...
		PrivilegedExceptionAction<AccessControlContext> act_super = new PrivilegedExceptionAction<AccessControlContext> () {
			public AccessControlContext run () throws Exception {
				return AccessController.getContext ();
			}
		};
		..*/
		
		try {
			/*..
			return Subject.doAsPrivileged ( oj_caller, 
								  act_run,
								  null //AccessController.doPrivileged( act_super )
								  );
			..*/
			return AccessController.doPrivileged ( new MakePrivilegedAction ( act_run ) );
		} catch ( PrivilegedActionException e ) {
			throw e.getCause ();
		} finally {
			long l_runtime = timer_run.sample ();
			ostat_call.sample ( (float) l_runtime );
			String s_caller = "nobody";
			
			if ( null != oj_caller ) {
				s_caller = oj_caller.toString ();
			}
			olog_call.log ( Level.FINE, method_call.toString () + " by " + s_caller +
							 " in " + l_runtime + "ms"
							 );
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

