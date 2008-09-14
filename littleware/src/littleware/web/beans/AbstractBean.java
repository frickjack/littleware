/*
 * Copyright (c) 2007,2008 Reuben Pasquini (catdogboy@yahoo.com)
 * All Rights Reserved
 */

package littleware.web.beans;


/**
 * Base class with shared functionality for various form beans.
 */
public class AbstractBean {
    private SessionBean   osession = null;
    
    /**
     * SessionBean property should be set at initialization
     * by JSF engine for use by form actions.
     */
    public SessionBean  getSessionBean () { return osession; }
    public void setSessionBean ( SessionBean session ) {
        osession = session;
    }
    
    private ActionResult   oresult_last = null;
    
    /**
     * LastResult property set by subtypes on completion of a
     * bean action method.
     */
    public ActionResult getLastResult () {
        return oresult_last;
    }
    /**
     * LastResult setter return result_last.toString () 
     * or null if result_last is null as a convenience
     * for action methods returning a string -
     *    return setLastResult ( result_my );
     */
    public String setLastResult ( ActionResult result_last ) {
        oresult_last = result_last;
        return ((null == result_last) ? null : oresult_last.toString ());
    }

    
}
