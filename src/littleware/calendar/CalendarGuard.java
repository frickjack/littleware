package littleware.calendar;


/**
 * Guard determines whether the user (passed to the constructor)
 * has access to the specified Calendar object.
 */
public class CalendarGuard implements java.security.Guard {

	/** 
	 * Determines whether the user associated with this guard
	 * has permissions to access the given calendar
	 *
	 * @param x_calendar to check for access-rights to
	 * @exception SecurityException if rights not available
	 */
	public void checkGuard ( Object x_calendar ) throws ( SecurityException ) {
	}
}
