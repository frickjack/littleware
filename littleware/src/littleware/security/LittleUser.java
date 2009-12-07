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

/**
 * Specialization of LittlePrincipal with a few
 * more user specific methods not available on groups.
 */
public interface LittleUser extends LittlePrincipal {

    /**
     * Little principal-status class
     */
    public static enum Status {
        ACTIVE,
        INACTIVE
    }

    /** Maps getValue() to a UserStatus */
    public Status getStatus();

    /** Maps to setValue() */
    public void setStatus(Status n_status);

    /** Covariant return-type clone */
    @Override
    public LittleUser clone();
}

