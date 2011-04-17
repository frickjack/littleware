/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.internal;

import littleware.security.LittleRole;



/**
 * Stole this from the jakarta-slide project
 */
public class SimpleRole implements LittleRole {
    
    private final String os_name;
    
    public SimpleRole(String s_name) {
        os_name = s_name;
    }
    
    public String getName() {
        return os_name;
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
    
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean equals( Object x_other ) {
        if ( ! (x_other instanceof SimpleRole) ) {
            return false;
        }
        final SimpleRole role_other = (SimpleRole) x_other;
        return role_other.getName().equals(getName());
    }
}    
