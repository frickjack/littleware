package littleware.calendar;

import littleware.security.*;

/**
 * Interface for calendar managers.
 */
public interface CalendarManager {

    /**
     * Get the calendar with the given name.
     *
     * @param s_name name of the calendar to retrieve
     * @exception CalendarException if no such calendar exists
	 * @exception AccessException if active login context does not
	 *               have access to the named calendar
     */
     public Calendar getCalendar ( String s_name ) throws ( CalendarException, AccessException );
     
     /**
      * Register the given calendar with the calendar database
      *
      * @param x_calendar to register
      * @exception CalendarException if calendar does not have a unique name or
      *                  other failure condition
      */
      public void registerNewCalendar ( Calendar x_calendar ) throws CalendarException;
     
    /**
     * Return a set of rules that constrain the given rule to
     * so that it can be satisified by empty time slots in the given
     * list of calendars.
     */
    public List constrainRule ( Rule x_rule, int i_num_results, boolean b_best_fit,
                                CalendarSet v_calendars ) throws ( CalendarException );

}
