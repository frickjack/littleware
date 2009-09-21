/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security;

import java.util.UUID;

/**
 * Simple implementation of the SimpleUser interface
 */
public class SimpleUser extends SimplePrincipal implements LittleUser {

    /** Do-nothing constructor for java.io.Serializable */
    public SimpleUser() {
        setAssetType(SecurityAssetType.USER);
    }

    @Override
    public Status getStatus() {
        if (getState() == Status.ACTIVE.ordinal()) {
            return Status.ACTIVE;
        }
        return Status.INACTIVE;
    }

    @Override
    public void setStatus(Status n_status) {
        setState(n_status.ordinal());
    }

    /**
     * Basic initializer just sets the principal name
     *
     * @param s_name must be alpha-numeric
     * @param u_id littleware id number
     * @param s_comment attached to user
     */
    public SimpleUser(String s_name, UUID u_id, String s_comment) {
        super(s_name, u_id, s_comment);
        setAssetType(SecurityAssetType.USER);
    }

    /**
     * Return a simple copy of this object
     */
    @Override
    public SimpleUser clone() {
        return (SimpleUser) super.clone();
    }
}

