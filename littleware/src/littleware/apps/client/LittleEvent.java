package littleware.apps.client;

import java.util.EventObject;

/**
 * Event base-type propagated by LittleTool swing widgets
 * to notify UI handlers and controllers implementing the
 * LittleListener interface of the occurence of 
 * various littleware API driven events.
 */
public class LittleEvent extends EventObject {
	private String  os_operation = null;
	private Object  ox_result = null;
	private boolean ob_successful = true;
	
	/**
	 * Setup the LittleEvent with proper source and operation
	 * with a successful null result.
	 *
	 * @param x_source of the event
	 * @param s_operation whose result the event is notifying us of
	 */
	public LittleEvent ( Object x_source, String s_operation ) {
		super ( x_source );
	}
	
	/**
	 * Setup event with a result that is considered
	 * successful if x_result null or is not an instanceof Exception.
	 *
	 * @param x_source of the event
	 * @param s_operation whose result the event is notifying us of
	 * @param x_result of s_operation - Exception type will set isSuccessful() false
	 */
	public LittleEvent ( Object x_source, String s_operation, Object x_result ) {
		super ( x_source );
		os_operation = s_operation;
		ox_result = x_result;
		
		if ( (null != x_result) 
			 && (x_result instanceof Exception)
			 ) {
			ob_successful = false;
		}
	}
	
	/**
	 * Get the name of the operation associated with this LittleEvent
	 */
	public String getOperation () { return os_operation; }
	
	/**
	 * Return true if the operation result associated with this event
	 * is null or not an instanceof Exception.
	 */
	public boolean isSuccessful () { return ob_successful; }
	
	/**
	 * Get the result object associated with this event.
	 * May be null or an Exception type.
	 */
	public Object getResult () { return ox_result; }

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

