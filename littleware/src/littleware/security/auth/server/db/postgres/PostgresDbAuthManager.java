package littleware.security.auth.server.db.postgres;

import com.google.inject.Inject;
import java.util.*;

import littleware.asset.server.TransactionManager;
import littleware.security.auth.server.db.*;
import littleware.db.*;

/**
 * Postgres implementation of DbPasswordManager
 */
public class PostgresDbAuthManager implements DbAuthManager {
    private final TransactionManager   omgr_trans;

	/** Do nothing constructor */
    @Inject
	public PostgresDbAuthManager ( TransactionManager mgr_trans ) {
        omgr_trans = mgr_trans;
    }

 	public DbWriter<String> makeDbPasswordSaver ( UUID u_user )
	{
		return new DbPasswordSaver ( u_user, omgr_trans );
	}
	

	public DbReader<Boolean,String> makeDbPasswordLoader ( UUID u_user )
	{
		return new DbPasswordLoader ( u_user, omgr_trans );
	}
}	
