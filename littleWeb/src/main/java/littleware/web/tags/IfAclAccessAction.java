/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.tags;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import littleware.asset.client.AssetSearchManager;
import littleware.security.*;

/** 
 * JSP tag-action supporting the <i>littleware:ifAclRead</i> tag.
 */
public class IfAclAccessAction extends ConditionalTagSupport {

    private static final Logger log = Logger.getLogger("littleware.web.tags.IfAclAccessAction");
    private String os_acl = "acl.littleware.web.read";
    private LittlePermission operm_access = LittlePermission.WRITE;
    private AssetSearchManager searchMgr;

    /** Do nothing constructor */
    public IfAclAccessAction() {
    }

    /** Set the ACL name - default is acl.littleware.web.read */
    public void setAcl(String s_acl) {
        os_acl = s_acl;
    }

    /**
     * Set the type of access required
     *
     * @param s_access must be read or write - assumed
     *                  write if neither
     */
    public void setAccess(String s_access) {
        if (s_access.equals("read")) {
            operm_access = LittlePermission.READ;
        } else {
            operm_access = LittlePermission.WRITE;
            if (!s_access.equals("write")) {
                log.log(Level.WARNING, "Unknown permission name requested: " + s_access);
            }
        }
    }

    /**
     * Set the SessionHelper by which we can access the littleware database.
     */
    public void setHelper(AssetSearchManager searchMgr) {
        this.searchMgr = searchMgr;
    }

    /**
     * Return true if the current user (Subject.getSubject)
     * has this bean&apos;s access permission on this bean&apos;s
     * acl.  Return false if the ACL does not exist, or
     * no user is authenticated.
     *
     * @throws JspTagException on failure to access littleware backend
     */
    @Override
    public boolean condition() throws JspTagException {

        try {
            LittleUser p_caller = null;

            if (null == p_caller) {
                log.log(Level.INFO, "unable to retrive LitteUser for ACL check");
                return false;
            }

            final LittleAcl acl_check = searchMgr.getByName(os_acl, LittleAcl.ACL_TYPE).get().narrow();
            return acl_check.checkPermission(p_caller, operm_access);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JspTagException("Caught unexpected: " + e, e);
        }

    }
}
