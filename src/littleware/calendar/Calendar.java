package littleware.calendar;

import littleware.security.*;
import java.util.*;


/**
 * Basic clanedar interface for manipulating Calendar events
 * on a particular named calendar.  Assume that some CalendarManager
 * takes care of authenticating users and exporting management services.
 */
public interface Calendar {

/**
 * Every calendar has a name which must be globally unique 
 * in the local database.
 *
 * @param s_name to give the calendar
 * @exception CalendarException if name is not legal
 */
public void setName ( String s_name ) throws ( CalendarException, AccessException );

/**
 * Get the name currently assigned to this calendar.
 * Every calendar object is assigned a default name by its constructor
 */
public String getName ();

/**
 * Get the time-ordered list of events scheduled on the calendar
 * for the given time period.
 *
 * @param t_start
 * @param t_end
 * @return list of events
 * @exception CalendarException if t_start > t_end or other failure condition
 */
public List getEvents ( Date t_start, Date t_end ) throws ( CalendarException );

/**
 * Add a rule to the calendar.  A rule is a simple state machine that
 * genearates calendar events.  For example, a typical rule might be:
 *      
 *      "Joanne's Birthday all-day 01/21/* (every year)"
 *
 * @param x_rule to add to the calendar
 * @return the unique ID assigned to the rule by the calendar backend
 * @exception CalendarException if unable to evaluate rule
 */
public UniqueID addRule ( Rule x_rule ) throws ( CalendarException, AccessException );

/**
 * Get the rule with the given UniqueID
 *
 * @param UniqueID to lookup
 * @return Rule with the given ID
 * @exception if no such rule is located
 */
 public Rule getRule ( UniqueID x_id ) throws ( CalendarException );
 
 /**
  * Update the given rule with new data
  *
  * @param x_rule with valid data to update including getUniqueID field
  * @exception CalendarException if unable to update rule with given new data
  */
  public void updateRule ( Rule x_rule ) throws ( CalendarException, AccessException );
  
  /**
   * Get the set of users/groups that have varioius forms of access to this calendar
   *
   * @return a reference to this calendar's access list
   * @exception AccessException if not allowed to access this calendar's ACL
   */
   public Acl getAcl () throws ( AccessException );
}
