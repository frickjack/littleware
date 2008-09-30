package littleware.security;

import java.security.acl.Permission;
import java.util.UUID;
import java.util.Set;

import littleware.base.DynamicEnum;
import littleware.base.UUIDFactory;
import littleware.base.NoSuchThingException;
import littleware.base.AccessPermission;


/**
 * Simple permission implementation of java.security.acl.Permission
 */
public class LittlePermission extends DynamicEnum<LittlePermission> implements java.security.acl.Permission {
	/** Shortcut to DynamicEnum.getMembers */
	public static Set<LittlePermission> getMembers () { return getMembers ( LittlePermission.class ); }
	
	/** Shortcut to DynamicEnum.getMember */
	public static LittlePermission getMember ( UUID u_id ) throws NoSuchThingException { 
		return getMember ( u_id, LittlePermission.class ); 
	}
	
	/** Shortcut to DynamicEnum.getMember */
	public static LittlePermission getMember ( String s_name ) throws NoSuchThingException { 
		return getMember ( s_name, LittlePermission.class ); 
	}

	protected LittlePermission () {}
	protected LittlePermission ( UUID u_id, String s_name ) {
		super ( u_id, s_name, LittlePermission.class, 
				new AccessPermission ( "newpermission" )
				);
	}
	
	public static final LittlePermission READ = new LittlePermission ( UUIDFactory.parseUUID ( "EEB72C11DE934015BE42FA6FA9423EAC" ),
													  "littleware.READ"
													  );
	public static final LittlePermission WRITE = new LittlePermission ( UUIDFactory.parseUUID ( "55D1BF9F49234D839B56354BC2F2BA90" ),
													  "littleware.WRITE"
													  );	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

