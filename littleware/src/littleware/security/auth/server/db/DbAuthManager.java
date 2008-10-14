package littleware.security.auth.server.db;

import java.util.*;

import littleware.db.*;

/**
 * Interface abstracts away littleware.security.auth db interactions
 */
public interface DbAuthManager {
	
	/**
	 * Create password-creator handler
	 *
	 * @param u_user id of user to update the password for
	 */
	public DbWriter<String> makeDbPasswordSaver ( UUID u_user );
	
	/**
	 * Create password-loader handler
	 *
	 * @return reader that takes the password as an arg, and returns
	 *               true if the password authenticates
	 */
	public DbReader<Boolean,String> makeDbPasswordLoader ( UUID u_user );	
	
}	
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

