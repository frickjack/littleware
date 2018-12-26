package littleware.security;

import java.util.UUID;
import java.util.Set;

import littleware.base.DynamicEnum;
import littleware.base.UUIDFactory;


/**
 * Simple permission implementation of java.security.acl.Permission
 */
public class LittlePermission extends DynamicEnum<LittlePermission> {
	private static final long serialVersionUID = 1L;

	/** Shortcut to DynamicEnum.getMembers */
	public static Set<LittlePermission> getMembers () { return getMembers ( LittlePermission.class ); }
	
	/** Shortcut to DynamicEnum.getMember */
	public static LittlePermission getMember ( UUID id ) { 
		return getMember ( id, LittlePermission.class ); 
	}
	
	/** Shortcut to DynamicEnum.getMember */
	public static LittlePermission getMember ( String name ) { 
		return getMember ( name, LittlePermission.class ); 
	}

	protected LittlePermission () {}
	protected LittlePermission ( UUID id, String name ) {
		super ( id, name, LittlePermission.class);
	}
	
	public static final LittlePermission READ = new LittlePermission ( UUIDFactory.parseUUID ( "EEB72C11DE934015BE42FA6FA9423EAC" ),
													  "littleware.READ"
													  );
	public static final LittlePermission WRITE = new LittlePermission ( UUIDFactory.parseUUID ( "55D1BF9F49234D839B56354BC2F2BA90" ),
													  "littleware.WRITE"
													  );	
}
