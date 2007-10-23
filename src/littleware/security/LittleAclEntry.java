package littleware.security;

import java.security.acl.AclEntry;

import littleware.asset.Asset;


/**
 * Interface exported by littleware AclEntry Asset.
 * Not intended to be saved/read directly by clients -
 * rather loaded/saved as part of a LittleAcl - which
 * takes care of setting owner, home, from, to, etc.
 * attributes to be consistent with the Acl it belongs to.
 * NOTE: each entry can belong to only one ACL
 */
public interface LittleAclEntry extends AclEntry, Asset {
    /** Covariant return-type clone */
    public LittleAclEntry clone ();

    /**
     * Set this entry read-only.  Once set cannot be undone
	 */
	public void setReadOnly ();
    
    /** Is this entry ReadOnly ? */
    public boolean isReadOnly ();
    
    /**
     * Covariant return-type: LittlePrincipal
     */
    public LittlePrincipal getPrincipal ();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

