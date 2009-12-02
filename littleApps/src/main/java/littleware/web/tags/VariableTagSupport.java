//
//  VariableTagSupport.java
//  littleware
//
//  Created by Reuben Pasquini on 11/04/2007.
//  Copyright 2007 frickjack.com. All rights reserved.
//

package littleware.web.tags;


import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;


/**
 * Base class for tag classes that export a variable to the JSP context.
 * Sets up var and scope properties, and provides an export( x_value ) method
 * to export an object to the JSP var property in the specified scope.
 * Subtypes must still implement doTag.  Stole idea form Oracle blog:
 *   http://www.oracle.com/technology/pub/articles/andrei_jsptags.html
 */
public abstract class VariableTagSupport extends SimpleTagSupport {
 
    private String os_var = null;
    
    /**
     * Set the property tracking the JSP context variable
     * (in scope getScope) in which to stash the
     * result of this tag action.
     */
    public void setVar ( String s_var ) {
        os_var = s_var;
    }
    public String getVar () { return os_var; }
    
    private int oi_scope = PageContext.REQUEST_SCOPE;
    
    /** 
     * Scope property for output variable - default to request
     *
     * @JspException if invalid scope specified
     */
    public void setScope(String s_scope) throws JspException {
        if (s_scope.equalsIgnoreCase("page")) {
            oi_scope = PageContext.PAGE_SCOPE;
        } else if (s_scope.equalsIgnoreCase("request")) {
            oi_scope = PageContext.REQUEST_SCOPE;
        } else if (s_scope.equalsIgnoreCase("session")) {
            oi_scope = PageContext.SESSION_SCOPE;
        } else if (s_scope.equalsIgnoreCase("application")) {
            oi_scope = PageContext.APPLICATION_SCOPE;
        } else {
            throw new JspException("Invalid scope: " + s_scope);
        }
    }    
    /** Getter returns one of the PageContext.*_SCOPY constants */
    public int getScope () { return oi_scope; }
    
    /**
     * Export the given object to the getVar() JSP context
     * variable in the getScope() scope if getVar() is not null.
     * Remove the variable from context if value is null.
     * 
     * @param value to stuff into the JSP environment
     * @return true of JSP context updated
     */
    protected boolean export(Object value) {
        if ( getVar () == null) {
            return false;
        }
        JspContext jspContext = getJspContext();
        if (value != null) {
            jspContext.setAttribute( getVar (), value, getScope () );
        } else {
            jspContext.removeAttribute(getVar (), getScope () );
        }
        return true;
    }    
    
}
