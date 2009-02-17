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

import java.security.acl.Acl;
import java.security.Principal;

import littleware.asset.Asset;

/**
 * Slight specialization of Acl to incorporate into littleware Asset framework.
 * Ignores the p_owner, p_caller arguments, and does not perform Owner checks on methods -
 * assumes Owner is ok.
 * Security check takes place when client tries to save ACL mods back to the
 * Littleware repository.
 * Override Acl methods with no-exception versions.
 */
public interface LittleAcl extends Acl, Asset {
    public final static String ACL_EVERYBODY_READ = "acl.littleware.everybody.read";

    /** Covariant return-type clone */
    public LittleAcl clone ();
    
    /** Convenience method - clear all the entries from the ACL */
    public void clearEntries ();
    
    /**
     * Throws UnsupportedOperationException - do getOwner(...).isOwner(...) instead
     */
    public boolean isOwner ( Principal p_owner );
    
    /**
     * Throws UnsupportedOperationException - do getOwner(...)... instead
     */
	public boolean deleteOwner ( Principal p_caller, Principal p_owner );
    
    /**
     * Throws UnsupportedOperationException - do getOwner(...)... instead
     */
    public boolean addOwner ( Principal p_caller, Principal p_owner );

    /**
     * Utility since our Acl implementation does not care who the caller is
     */
    public boolean removeEntry ( LittleAclEntry x_entry );
    
    /**
     * Utility since our Acl implementation does not care who the caller is
     */    
    public boolean addEntry ( LittleAclEntry x_entry );
    
    /**
     * Little utility - get the entry associated with the given Principal,
     * or return null if no entry registered.
     *
     * @param p_entry Principal we want to get the entry for
     * @param b_negative do we want the postive or negative entry ?
     * @return p_entry's entry or null if p_entry entry not in this Acl
     */
    public LittleAclEntry getEntry ( Principal p_entry, boolean b_negative );
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

