package littleware.base;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;


/**
 * Little utility class to fascilitate code reuse
 * between methods that want to reconnect and retry
 * when catching a RemoteException.
 * In general subtypes should override handle()
 * with something that invokes super.handle( e ),
 * then invokes situation specific retry logic
 * if an exception is not thrown by super.
 */
public class RemoteExceptionHandler {
	private static Logger olog_generic = Logger.getLogger ( "littleware.base.RemoteExceptionHandler" );
	
	/**
     * Default number of times client may invoke
	 * handle( RemoteException e ) before handle just starts throwing
	 * the RemoteException.
	 */
	public final static int DEFAULT_MAX_CALLS = 3;
	/**
	 * Default number of seconds to sleep after incrementing
	 * the retry count if the retry count does not exceed
	 * the object maximum
	 */
	public final static long DEFAULT_SLEEP_MS = 3000;
	
	private int  oi_max = DEFAULT_MAX_CALLS;
	private long ol_sleep_ms = DEFAULT_SLEEP_MS;
	private int  oi_count = 0;
	
	
	/**
	 * Do nothing constructor - sets up defaults values
	 */
	public RemoteExceptionHandler () {}
	
	/**
	 * Constructor allows override of max calls to handle, 
	 * and handle-sleep.
	 *
	 * @param l_sleep_ms no sleep if <= 0
	 */
	public RemoteExceptionHandler ( int i_max, long l_sleep_ms ) {
		oi_max = i_max;
		ol_sleep_ms = l_sleep_ms;
	}
	
	/**
	 * Get count of handle() calls so far
	 */
	public int getHandleCount () { return oi_count; }
	/** Get limit on handle calls before handle just throws its RemoteException argument */
	public int getHandleMax () { return oi_max; }
	/** Get the ms handle() sleeps if it does not throw the RemoteException */
	public long getSleepMs () { return ol_sleep_ms; }
	
	/**
	 * Handler increments count, throws RemoteException argument if
	 * count exceeds max, otherwise logs the exception with a stack trace,
	 * and sleeps for configured period.
	 *
	 * @param e_in exception to throw if count exceeds max
	 */
	public void handle ( RemoteException e_in ) throws RemoteException {
		++oi_count;
		if ( oi_count > oi_max ) {
			throw e_in;
		}
		olog_generic.log ( Level.INFO, "RemoteException retry count " + oi_count + ": " +
						   e_in + ", " + BaseException.getStackTrace ( e_in )
						   );
		if ( ol_sleep_ms > 0 ) {
			try {
				Thread.sleep ( ol_sleep_ms );
			} catch ( InterruptedException e ) {
				olog_generic.log ( Level.INFO, "Sleep interrupted, caught: " + e );
			}
		}
	}
}
		
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

