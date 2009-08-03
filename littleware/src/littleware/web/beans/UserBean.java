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
import littleware.security.LittleUser;

/**
 * Session scope bean gives access to logged in user
 */
public class UserBean extends InjectMeBean {
    private LittleUser user;

    @Inject
    public void setUser( LittleUser user ) { this.user = user; }
    public LittleUser getUser() { return user; }
}
