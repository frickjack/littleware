package littleware.security;

import java.security.Principal;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.acl.*;
import java.util.*;

import littleware.base.Whatever;
import littleware.asset.Asset;
import littleware.asset.InvalidAssetTypeException;


/**
 * Implement java.security.Group interface for tracking groups as principles in 
 * the littleware framework.
 * Littleware requires that user-names and group-names be globally
 * unique across both sets.
 * Permission protected class - clients should only be able to create
 * this thing via a littleware.db.GroupManager instance.
 * The members of group.littleware.administrator, the group
 * creator, X for group.X groups, and the group's owner 
 * (see editGroup) are the owners of a group.
 */
public class SimpleGroup extends SimplePrincipal implements LittleGroup {
	private HashSet<LittleUser>      ov_members   = new HashSet<LittleUser> ();
	private HashSet<LittleGroup>     ov_subgroups = new HashSet<LittleGroup> ();
	
	// Internal dynamic lookup cache - saves results of one lookup for reuse next time
	private HashSet<LittlePrincipal> ov_member_cache = new HashSet<LittlePrincipal> ();
	// If we test for a member not in this group, then we know the cache is complete
	private boolean              ob_cache_complete = false;
	
	/**
	 * Do nothing internal constructor for NULL-group needed by SimpleOwner 
	 * and java.io.Serializable
	 */
	public SimpleGroup () {
		setAssetType ( SecurityAssetType.GROUP );
	}
	
	/**
	 * Initializer given reference to group owner
	 *
	 * @param s_name of the group
	 * @param u_id of the group
	 * @param s_comment attached to the group
	 */
	public SimpleGroup ( String s_name, UUID u_id, String s_comment ) {
		super ( s_name, u_id, s_comment );
		setAssetType ( SecurityAssetType.GROUP );
	}
		
	
	
	
	
	/**
	 * Add a new member to the group.
	 * This implementation always throws an exception for
	 * code that does not hold the AccessPermission -
	 * must go through SecurityManager to manipulate group membership.
	 *
	 * @param p_member Principle to add to the group
	 * @return true if p_member successfully added, false if already a member
	 */
	public synchronized boolean addMember ( Principal p_member ) {
		if ( this.equals ( p_member ) ) {
			return false;
		}
		
		if ( p_member instanceof LittleGroup ) {
			if ( ov_subgroups.add ( (LittleGroup) p_member ) ) {
				// new member data - make sure the cache is marked incomplete
				ob_cache_complete = false;
				return true;
			}
			return false;
		} else { 
			// else - it's a USER, not a GROUP
			ov_member_cache.add ( (LittleUser) p_member );
			return ov_members.add ( (LittleUser) p_member );
		}
	}
	
	/** 
	 * Recursive internal procedure with extra argument to detect
	 * membership cycles. 
	 *
	 * @param p_member principal we're looking for
	 * @param v_already set of principals already visited -
	 *         contains contents of subgroups scanned after
	 *         recursion finishes.
	 */
	private boolean isMember ( Principal p_member, Set<LittlePrincipal> v_already ) {
		if ( v_already.contains ( this ) ) {
			return false;
		}
		v_already.add ( this );
		
		boolean b_result = false;
		
		if ( this.equals ( p_member ) 
			 || ov_members.contains ( p_member ) 
			 || ov_subgroups.contains( p_member ) 
			 ) 
		{
			b_result = true;
		} else {		
			for ( Iterator<LittleGroup> r_i = ov_subgroups.iterator ();
				  r_i.hasNext () && (! b_result);
				  ) {
				SimpleGroup p_subgroup = (SimpleGroup) r_i.next ();
				if ( p_subgroup.isMember ( p_member, v_already ) ) {
					b_result = true;
				}
			}
		}
		v_already.addAll ( ov_members );
		return b_result;
	}
	
	/**
	 * Does a closure on the freakin' subgroups to search for the requested member.
	 * Caches data from the search in an internal cache that gets
	 * flushed if a 'remove' operation gets performed.
	 * Method is syncrhonized to avoid issues manipulating that internal cache.
	 */
	public synchronized boolean isMember ( Principal p_member ) {
		boolean b_in_cache = ov_member_cache.contains ( p_member );
		if ( b_in_cache || ob_cache_complete ) {
			return b_in_cache;
		}
		boolean b_result = isMember ( p_member, ov_member_cache );
		if ( ! b_result ) { // must have scanned everything looking
			ob_cache_complete = true;
		}
		return b_result;
	}
	
	
	/**
	 * Get an enumeration of the group members
	 *
	 * @return enumeration
	 */
	public synchronized Enumeration<? extends Principal> members () {
		List<LittlePrincipal>   v_result = new ArrayList<LittlePrincipal > ();
		v_result.addAll ( ov_members );
		v_result.addAll ( ov_subgroups );
		return Collections.enumeration ( v_result );
	}
	

	public synchronized boolean removeMember ( Principal p_member ) {
		boolean b_result = false;
		if ( p_member instanceof Group ) {
			b_result = ov_subgroups.remove ( p_member );
			if ( b_result ) {
				// Need to clear out the lookup cache if a whole group gets removed
				ov_member_cache.clear ();
				ob_cache_complete = false;
			}
		} else {
			b_result = ov_members.remove ( p_member );
			if ( b_result ) {
				// Just remove that one user from the lookup cache
				ov_member_cache.remove ( p_member );
			}
		}	 
		return b_result;
	}
	
    public synchronized void clearMembers () {
        ov_subgroups.clear ();
        ov_members.clear ();
        ov_member_cache.clear ();
        ob_cache_complete = true;
    }
	
	/**
	 * Return a simple copy of this object - except setup new empty
	 * member sets.
	 */
	public synchronized SimpleGroup clone () {
		SimpleGroup grp_copy = (SimpleGroup) super.clone ();
		grp_copy.ov_members = (HashSet<LittleUser>) ov_members.clone ();
		grp_copy.ov_subgroups = (HashSet<LittleGroup>) ov_subgroups.clone ();
        grp_copy.ov_member_cache = (HashSet<LittlePrincipal>) ov_member_cache.clone ();
		return grp_copy;
	}
    
    public void sync ( Asset a_copy_source ) throws InvalidAssetTypeException {
        if ( this == a_copy_source ) {
            return;
        }
        super.sync ( a_copy_source );
		SimpleGroup grp_copy_source = (SimpleGroup) a_copy_source;
		ov_members = (HashSet<LittleUser>) grp_copy_source.ov_members.clone ();
		ov_subgroups = (HashSet<LittleGroup>) grp_copy_source.ov_subgroups.clone ();
        ov_member_cache = (HashSet<LittlePrincipal>) grp_copy_source.ov_member_cache.clone ();
        ob_cache_complete = grp_copy_source.ob_cache_complete;
    }        
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

