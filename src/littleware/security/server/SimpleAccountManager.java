package littleware.security.server;

import java.rmi.RemoteException;
import java.security.*;
import java.security.acl.*;
import javax.security.auth.*;
import java.security.GuardedObject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.SQLException;

import javax.sql.DataSource;

import littleware.base.*;
import littleware.db.*;
import littleware.asset.*;
import littleware.asset.server.TransactionManager;
import littleware.asset.server.LittleTransaction;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.security.auth.server.db.*;


/**
 * Simple security-manager implementation
 */
public class SimpleAccountManager extends NullAssetSpecializer implements AccountManager, AssetSpecializer {
	private static final Logger      olog_generic = Logger.getLogger ( "littleware.security.server.SimpleAccountManager" );
	
	private final AssetManager       om_asset;
	private final AssetSearchManager om_search;
	private final DbAuthManager      om_dbauth;

		
	
	/**
	 * Constructor injects dependencies
	 *
	 * @param m_asset asset manager
	 * @param m_searcher asset search manager
	 * @param m_dbauth access to password database handlers
	 */
	public SimpleAccountManager ( AssetManager m_asset, 
								  AssetSearchManager m_searcher,
								  DbAuthManager m_dbauth
                                  ) {
		om_asset = m_asset;
		om_search = m_searcher;
		om_dbauth = m_dbauth;
	}
	

	/**
	 * Specialize USER and GROUP type assets
	 *
	 * @param a_in asset instance of class returned by a_in.getAssetType ().create ()
	 * @return a_in decorated with new data, or a new Asset consistent with the data in a_in
	 */
    @Override
	public <T extends Asset> T narrow ( T a_in, AssetRetriever m_retriever
						  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( a_in.getAssetType ().equals ( SecurityAssetType.USER ) 
			 || a_in.getAssetType ().equals ( SecurityAssetType.QUOTA )
			 ) {
			return a_in;
		}
		Whatever.check ( "AccountManager is not the specializer for: " +
						 a_in.getAssetType (),
						 a_in.getAssetType ().equals ( SecurityAssetType.GROUP )
						 );
		olog_generic.log ( Level.FINE, "Narrowing group: " + a_in.getName () );
		
		// It's a GROUP - need to populate it
		LittleGroup       grp_result = (LittleGroup) a_in;
        grp_result.clearMembers ();   // clear cloned member list and rebuild
		Map<String,UUID>  v_links = m_retriever.getAssetIdsFrom ( grp_result.getObjectId (),
																   SecurityAssetType.GROUP_MEMBER
																   );
		
		Set<Asset> v_link_assets = m_retriever.getAssets ( v_links.values () );
		
        /** 
         * This check not true after a member group/user has been deleted.
         * Cleanup takes place at update time for this group.
         *
		Whatever.check ( "got expected number of assets: " + v_links.size () + " == " + v_link_assets.size (), 
						 v_links.size () == v_link_assets.size () 
						 );
        */
		olog_generic.log ( Level.FINE, "Group: " + a_in.getName () + " found " + v_link_assets.size () +
						   " children under " + a_in.getObjectId () + " of type " +
						   SecurityAssetType.GROUP_MEMBER.getObjectId ()
						   );

		
		Set<UUID> v_members = new HashSet<UUID> ();
		for ( Asset a_link : v_link_assets ) {
			olog_generic.log ( Level.FINE, "Got link UUID " + a_link.getToId () );
			v_members.add ( a_link.getToId () );
		}
		
		// Recursion here!
		Set<Asset> v_member_assets = m_retriever.getAssets ( v_members );
		
		for ( Asset a_member: v_member_assets ) {
			olog_generic.log ( Level.FINE, "adding " + a_member.getName () + " to " +
							   grp_result.getName ()
							   );
			if ( SecurityAssetType.USER.equals ( a_member.getAssetType () ) ) {
				grp_result.addMember ( (LittleUser) a_member );
			} else {
				grp_result.addMember( (LittleGroup) a_member );
			}
		}
		
		if ( grp_result.getName ().equals ( AccountManager.LITTLEWARE_ADMIN_GROUP ) ) {
			// then add the admin principal
			grp_result.addMember ( (LittleUser) m_retriever.getAsset ( AccountManager.UUID_ADMIN ) );
		}
		
		return a_in;
	}
	
	
	private static Subject oj_admin = null;
	
	/**
	 * Get a Subject representing the littleware admin
	 */
	private Subject getAdmin () throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == oj_admin ) {
			try {
				LittleUser p_admin  = (LittleUser) this.getPrincipal ( AccountManager.LITTLEWARE_ADMIN );
				Set<Principal> v_users = new HashSet<Principal> ();
				
				v_users.add ( p_admin );				
				oj_admin = new Subject ( true, v_users, new HashSet<Object>(), new HashSet<Object> () );
			} catch ( NoSuchThingException e ) {
				throw new AssertionFailedException ( "LITTLEWARE_ADMIN should exist, caught: " + e, e );
			} catch ( GeneralSecurityException e ) {
				throw new AssertionFailedException ( "LITTLEWARE_ADMIN should be accessible, but caught: " + e,
												 e );
			}
		}
		return oj_admin;
	}
	
	/**
	 * Little internal utility that creates the GROUP_MEMBER necessary
	 * to add the given member to the given group
	 *
	 * @param p_group to add to
	 * @param p_member to add to group
	 * @param m_asset manager to use to save the data
	 * @return the new group-member asset
	 */
	public Asset addMemberToGroup ( LittleGroup p_group, LittlePrincipal p_member,
									AssetManager m_asset
									) throws BaseException, AssetException, GeneralSecurityException,
		RemoteException
	{
		Asset a_link = SecurityAssetType.GROUP_MEMBER.create ();
		a_link.setName ( p_member.getName () );
		a_link.setFromId ( p_group.getObjectId () );
		a_link.setToId ( p_member.getObjectId () );
		a_link.setHomeId ( p_group.getHomeId () );
		a_link.setComment ( "Member of group: " + p_member.getName () );
		a_link = m_asset.saveAsset ( a_link, "new group member" );
		return a_link;
	}
	
	/**
	 * Little PrivilegedAction to add a member to a group as the ADMIN user
	 */
	public class AddToGroupAction implements PrivilegedExceptionAction<Asset> {
		private LittleGroup     op_group = null;
		private LittlePrincipal op_member = null;
		private AssetManager    om_asset  = null;
		
		public AddToGroupAction ( LittleGroup p_group, LittlePrincipal p_member,
								  AssetManager m_asset ) {
			op_group = p_group;
			op_member = p_member;
			om_asset = m_asset;
		}
		
		/** Create a GROUP_MEMBER asset linking this action's group and new member */
		public Asset run () throws BaseException, AssetException,
			GeneralSecurityException, RemoteException 
		{
			return addMemberToGroup ( op_group, op_member, om_asset );
		}
	}

	/**
	 * Specializer USER/GROUP updates. <br />
	 *  <ul>
	 *    <li> USER: setup QUOTA </li>
	 *    <li> GROUP: save members ? </li>
	 * </ul>
	 */
	public void postCreateCallback ( Asset a_new, AssetManager m_asset  							   
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( SecurityAssetType.USER.equals ( a_new.getAssetType () ) ) {
			// We need to setup a quota
			LittleUser p_caller = this.getAuthenticatedUser ();
			Quota      a_caller_quota  = this.getQuota ( p_caller );
			if ( null != a_caller_quota ) {
				Quota      a_quota    = (Quota) a_caller_quota.clone ();
				
				a_quota.setObjectId ( null );
				a_quota.setOwnerId ( UUID_ADMIN );
				a_quota.setFromId ( a_new.getObjectId () );
				a_quota.setToId ( a_caller_quota.getObjectId () );
				a_quota = (Quota) m_asset.saveAsset ( a_quota, "New quota" );
			}
			// Add this frickjack to the everybody group
			{
				LittleGroup p_everybody = (LittleGroup) this.getPrincipal ( AccountManager.LITTLEWARE_EVERYBODY_GROUP );
				
				try {
					PrivilegedExceptionAction act_add2group = new AddToGroupAction ( p_everybody,
																					 (LittlePrincipal) a_new,
																					 m_asset
																					 );
					Subject                   j_admin = getAdmin ();
					
					Subject.doAs ( j_admin, act_add2group );
				} catch ( PrivilegedActionException e ) {
					Exception e_cause = e.getException ();
					if ( e_cause instanceof BaseException ) {
						throw (BaseException) e_cause;
					}
					if ( e_cause instanceof GeneralSecurityException ) {
						throw (GeneralSecurityException) e_cause;
					}
					if ( e_cause instanceof AssetException ) {
						throw (AssetException) e_cause;
					}
					throw new AssertionFailedException ( "Exception of unexpected type", e_cause );
				}
			}
		} else if ( SecurityAssetType.GROUP.equals ( a_new.getAssetType () ) ) {
			postUpdateCallback ( null, a_new, m_asset );
		}
	}
	
	/**
	 * Delete group-member links to/from when a group or user gets deleted
	 */
	public void postDeleteCallback ( Asset a_deleted, AssetManager m_asset 
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( SecurityAssetType.GROUP.equals ( a_deleted.getAssetType () ) 
			 || SecurityAssetType.USER.equals ( a_deleted.getAssetType () ) 
			 ) {
			Map<String,UUID> v_children = om_search.getAssetIdsFrom ( a_deleted.getObjectId (),
																	  SecurityAssetType.GROUP_MEMBER
																	  );
			Set<Asset> v_member_links = om_search.getAssets ( v_children.values () );
			for ( Asset p_link : v_member_links ) {
				m_asset.deleteAsset ( p_link.getObjectId () , "cleaning up deleted principal" );
			}			
		}

	}
	
	
	/**
	 * Update group membership info when group asset gets updated
	 *
	 * @param a_pre_update may be null so that postCreateCallback
	 *              can leverage this
	 */
	public void postUpdateCallback ( Asset a_pre_update, Asset a_now, AssetManager m_asset 
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( SecurityAssetType.GROUP.equals ( a_now.getAssetType () ) ) {
			LittleGroup p_before = (LittleGroup) a_pre_update;
			LittleGroup p_after = (LittleGroup) a_now;
			
			Set<Principal>       v_before = new HashSet<Principal> ();
			
			if ( null != p_before ) {
				v_before.addAll ( Collections.list ( p_before.members () ) );
			}
			
			List<LittlePrincipal> v_add = new ArrayList<LittlePrincipal> ();
			Set<UUID>             v_members = new HashSet<UUID> ();

			// Get the list of new group members that need added
			for ( Enumeration< ? extends Principal > enum_new = p_after.members ();
				  enum_new.hasMoreElements ();
				  ) 
			{
				LittlePrincipal p_new = (LittlePrincipal) enum_new.nextElement ();
				
                v_members.add ( p_new.getObjectId () );
				if ( ! v_before.remove ( p_new ) ) {
					v_add.add ( (LittlePrincipal) p_new );
				}
			}
						
			{
				// Get the collection of assets linking the group-asset
				// to the group-members, and delete the unneeded ones
				Map<String,UUID> v_children = om_search.getAssetIdsFrom ( p_after.getObjectId (),
																		  SecurityAssetType.GROUP_MEMBER
																		  );
				Set<Asset> v_member_links = om_search.getAssets ( v_children.values () );
				for ( Asset a_link: v_member_links ) {
					if ( ! v_members.contains ( a_link.getToId () ) ) {
						m_asset.deleteAsset ( a_link.getObjectId (), "member no longer in group" );
					}
				}
			}
			
			// Add the new members to the group
			for ( LittlePrincipal p_add : v_add ) {
				addMemberToGroup ( p_after, p_add, m_asset );
			}
		}
	}
	
	
	/**
	 * PrivilgedAction for managing quota counter increments
	 */
	private class QuotaCheckAction implements PrivilegedExceptionAction<Integer> {
	    private       Quota            oa_quota = null;
		private final Date             ot_now = new Date ();
		
		public QuotaCheckAction ( Quota a_quota ) {
			oa_quota = a_quota;
		}
		
		/**
		 * Traverses the quota-chain recursively.
		 * Assumes the caller is already setuid to the administrator
		 *
		 * @return minimum ops remaining 
		 */
		public Integer run ( ) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
			int         i_ops_left = -1;
			List<Quota> v_chain    = new ArrayList<Quota> ();
            LittleTransaction trans_quota = TransactionManager.getTheThreadTransaction ();
                
            trans_quota.startDbAccess ();
            try {
                for ( Quota a_quota = oa_quota;
                      null != a_quota;
                      a_quota = (null != a_quota.getToId ()) ? ((Quota) om_search.getAsset ( a_quota.getToId () ))
                                                             : null
                      ) 
                {
                    v_chain.add ( a_quota );
                    if ( (null != a_quota.getEndDate ())
                         && (a_quota.getEndDate ().getTime () < ot_now.getTime ())
                         ) {
                        long l_period = a_quota.getEndDate ().getTime () -
                                            a_quota.getStartDate ().getTime ();
                        
                        if ( l_period < 1000000 ) {
                            l_period = 1000000;
                        }
                        Date t_end = new Date ( l_period + ot_now.getTime() );
                        a_quota.setStartDate ( ot_now );
                        a_quota.setEndDate ( t_end );
                        a_quota.setQuotaCount ( 0 );
                    } else if ( (a_quota.getQuotaLimit() >= 0)
                                && (a_quota.getQuotaLimit () < a_quota.getQuotaCount ())
                                ) {
                        throw new QuotaException ( "Quota exceeded: " + a_quota.getQuotaLimit () +
                                                   " less than " + a_quota.getQuotaCount ()
                                                   );
                    }
                    
                    int         i_left = a_quota.getQuotaLimit () - 
                        a_quota.getQuotaCount ( );
                    
                    if ( (i_left >= 0) && (i_left < i_ops_left) ) {
                        i_ops_left = i_left;
                    }
                }

                for ( Quota a_quota: v_chain ) {
                    a_quota.incrementQuotaCount ( );
                    // don't worry about missed transactions
                    a_quota.setTransactionCount ( 0 );
                    try {
                        olog_generic.log ( Level.FINE, "Incrementing quota count on " + a_quota.getObjectId () );
                        a_quota = (Quota) om_asset.saveAsset ( a_quota, "update quota count" );
                    } catch ( BaseException e ) {
                        throw (BaseException) e;
                    } catch ( GeneralSecurityException e ) {
                        throw (GeneralSecurityException) e;
                    } catch ( RemoteException e ) {
                        throw (RemoteException) e;
                    } catch ( Exception e ) {
                        throw new AssertionFailedException ( "Failed to increment quota", e );
                    }
                }
                return Integer.valueOf ( i_ops_left );
            } finally {
                trans_quota.endDbAccess ();
            }
		}
	}

	
	
	public int incrementQuotaCount () throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		LittleUser p_caller = this.getAuthenticatedUser ();
		Quota      a_quota = null;
		
		a_quota = this.getQuota ( p_caller );
		
		if ( null == a_quota ) {
			return -1;
		}
		try {
			Integer int_result = (Integer) Subject.doAs ( getAdmin (),
						   new QuotaCheckAction ( a_quota )
							);
			return int_result.intValue ();
		} catch ( PrivilegedActionException e ) {
			try {
				if ( e.getCause () instanceof Exception ) {
					throw (Exception) e.getCause ();
				} else {
					throw (Error) e.getCause ();
				}
			} catch ( DataAccessException e_cause ) {
				throw e_cause;
			} catch ( ManagerException e_cause ) {
				throw e_cause;
			} catch ( AssetException e_cause ) {
				throw e_cause;
			} catch ( Exception e_cause ) {
				throw new AssertionFailedException ( "Caught unexpected: " + e_cause, e_cause );
			}
		}		
	}

	/**
	 * Just call through to AssetSearchManager.getAsset () - 
	 * the AssetSearchManager calls back to our narrow() method
	 * since the active AccountManager (should be this)
	 * is registered as the AssetSpecializer for USER and GROUP type assets.
	 */
	public LittlePrincipal getPrincipal ( UUID u_id
										  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return (LittlePrincipal) om_search.getAsset ( u_id );
	}
	
	
	public LittlePrincipal getPrincipal ( String s_name
										  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
        LittleUser  user_result = om_search.getByName ( s_name, SecurityAssetType.USER );
        if ( null != user_result ) {
            return user_result;
        }
        LittleGroup group_result = om_search.getByName ( s_name, SecurityAssetType.GROUP );
        if ( null != group_result ) {
            return group_result;
        }
		throw new NoSuchThingException ( "No principal by name: " + s_name );
	}		
			
	private static Factory<UUID>  ofactory_uuid = UUIDFactory.getFactory ();	

	public LittleUser createUser (  LittleUser p_new,
							  String s_password
							  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		p_new.setObjectId ( ofactory_uuid.create () );
		p_new.setOwnerId ( p_new.getObjectId () );
		return updateUser ( p_new, s_password, "new user" );
	}


	public LittleUser updateUser ( LittleUser p_update, String s_password, 
							 String s_update_comment 
							 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
        if ( ! isValidPassword ( s_password ) ) {
			throw new IllegalNameException ( "Illegal password: " + s_password );
		}
        
        LittleTransaction  trans_update = TransactionManager.getTheThreadTransaction ();
        
        try {
            boolean b_rollback = true;
            
            trans_update.startDbUpdate ();
            try {
                p_update = (LittleUser) om_asset.saveAsset ( p_update, s_update_comment );
                try {
                    DbWriter<String> sql_password = om_dbauth.makeDbPasswordSaver ( p_update.getObjectId () );
                    sql_password.saveObject ( s_password );
                } catch ( SQLException e ) {
                    throw new DataAccessException ( "Falure updating password, caught: " + e, e );
                }
                b_rollback = false;
                return p_update;
            } finally {
                trans_update.endDbUpdate ( b_rollback );
            }
        } catch ( SQLException e ) {
            throw new DataAccessException ( "Unexpected SQLException", e );
        }

	}
	

	public boolean isValidPassword ( String s_password ) {
		if ( null == s_password ) { return false; }
		s_password = s_password.trim ();
		return (s_password.length () > 5);
	}
	

	public LittleUser getAuthenticatedUser () throws NotAuthenticatedException, ManagerException {
		LittleUser p_result = SecurityAssetType.getAuthenticatedUserOrNull ();
		
		if ( null == p_result ) {
			throw new NotAuthenticatedException ( "No user authenticated" );
		}
		return p_result;
	}
	
	
	public Quota getQuota ( LittleUser p_user ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		Map<String,UUID> v_quotas = om_search.getAssetIdsFrom ( p_user.getObjectId (),
													  SecurityAssetType.QUOTA
													  );
		UUID u_child = v_quotas.get ( "littleware_quota" );
		if ( null == u_child ) {
			return null;
		}
		
		return (Quota) om_search.getAssetOrNull ( u_child );
	}
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

