package littleware.security;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.security.*;
import java.security.acl.*;

import littleware.base.*;
import littleware.asset.*;


/**
 * Simple implementation of java.security.acl.Owner interface
 * that just wraps a Group that represents a set of owners.
 * The members of the group.littleware.admin group are automatically
 * added to every Owner.  Each object should know what users/groups
 * to add to its owner set at initialization time.
 */
public class SimpleOwner implements Owner, java.io.Serializable, Cloneable {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.security.SimpleOwner" );
	
	private     LittleGroup     ogroup_owner = SecurityAssetType.GROUP.create ();
	private     LittleGroup     ogroup_admin = SecurityAssetType.GROUP.create ();
    {
        ogroup_owner.setName ( "bogus-owner-group" );
        ogroup_admin.setName ( "bogus-admin-group" );
    }
	

	/**
     * Do-nothing constructor required for serializtion.
     */
    SimpleOwner () {}
	
    
    /**
     * Constructor with no non-admin owner
     */
    public SimpleOwner ( LittleGroup group_admin ) {
        this( null, group_admin );
    }
    
	/**
	 * Constructor sets up owner with gr_admin group and the specified Principal
	 *
	 * @param p_owner co-owner with admin group - null indicates no non-admin owner
     * @param group_admin that owns everything, and cannot be removed
	 */
	public SimpleOwner ( Principal p_owner, LittleGroup group_admin ) {
        if ( null != p_owner ) {
            ogroup_owner.addMember ( p_owner );
        }
        ogroup_admin = group_admin;
	}

	public boolean addOwner ( Principal p_caller, Principal p_owner ) throws NotOwnerException {
		if ( ! isOwner ( p_caller ) ) {
			throw new NotOwnerException ();
		}
		return ogroup_owner.addMember ( p_owner );
 	}
	

	public boolean deleteOwner ( Principal p_caller, Principal p_owner 
								 ) throws NotOwnerException, LastOwnerException 
	{
		if ( ! isOwner ( p_caller ) ) {
			throw new NotOwnerException ();
		}
		
		return ogroup_owner.removeMember ( p_owner );
	}
	
	/**
	 * Return true if given principal is an owner or member of admin group,
	 * false otherwise.  If p_owner is null, then test the active JAAS Principal.
	 */
	public boolean isOwner ( Principal p_owner ) {
		if ( null == p_owner ) {
			p_owner = SecurityAssetType.getAuthenticatedUserOrNull ();
		}
		if ( (null == p_owner) 
			 || (! (p_owner instanceof LittleUser) )
			 ) {
			return false;
		}
		
		return (
				AccountManager.UUID_ADMIN.equals ( ((LittleUser) p_owner).getObjectId () )
				|| ogroup_owner.isMember ( p_owner )
				|| ogroup_admin.isMember ( p_owner )
				);
	}
    
    public SimpleOwner clone () {
        try {
            SimpleOwner owner_clone = (SimpleOwner) super.clone ();
            owner_clone.ogroup_owner = ogroup_owner.clone ();
            return owner_clone;
        } catch ( CloneNotSupportedException e ) {
            throw new AssertionFailedException ( "Clone should be supported here", e );
        }
    }
    
    public void sync ( SimpleOwner a_copy_source ) throws InvalidAssetTypeException {
        if ( this == a_copy_source ) {
            return;
        }
        SimpleOwner owner_copy_source = (SimpleOwner) a_copy_source;
        ogroup_owner = owner_copy_source.ogroup_owner.clone ();
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

