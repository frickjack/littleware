package littleware.web.tags;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;



/**
* Code for JSP cache-tag stolen from onjava.com
*    <a href="http://www.onjava.com/pub/a/onjava/2005/01/05/jspcache.html"> 
*          http://www.onjava.com/pub/a/onjava/2005/01/05/jspcache.html
*    </a>
* , and customized.
*/
public class JspUtils {
    /**
     * Convert a s_scope-name string to a s_scope index
     *
     * @exception IllegalArgumentException if invalid scope given
     */
    public static int checkScope(String s_scope) {
        if ("page".equalsIgnoreCase(s_scope)) {
            return PageContext.PAGE_SCOPE;
        } else if ("request".equalsIgnoreCase(s_scope)) {
            return PageContext.REQUEST_SCOPE;
        } else if ("session".equalsIgnoreCase(s_scope)) {
            return PageContext.SESSION_SCOPE;
        } else if ("application".equalsIgnoreCase(s_scope)) {
            return PageContext.APPLICATION_SCOPE;
        } else {
            throw new IllegalArgumentException(
                "Invalid s_scope: " + s_scope);
        }
    }

}
