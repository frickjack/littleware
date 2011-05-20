/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.beans;

import com.google.inject.Inject;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import littleware.asset.client.AssetSearchManager;
import littleware.base.AssertionFailedException;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.LittleUser;

/**
 * Session scope bean gives access to logged in user
 */
@ManagedBean
@SessionScoped
public class UserBean extends InjectMeBean {
    private LittleUser user = null;
    private boolean    admin = false;

    public LittleUser getUser() { return user; }
    /** Is this user a member of the littleware admin group ? */
    public boolean    isAdmin() { return admin; }
    

    @Inject
    public void injectMe( LittleUser user, AssetSearchManager search ) {
        this.user = user;
        try {
            this.admin = search.getByName( AccountManager.LITTLEWARE_ADMIN_GROUP, LittleGroup.GROUP_TYPE ).get().narrow( LittleGroup.class ).isMember( user );
        } catch ( Exception ex ) {
            throw new AssertionFailedException( "Failed to load admin group", ex );
        }
    }
    
}
