package littleware.asset.server.db.jpa;

import javax.persistence.EntityManager;
import littleware.asset.server.LittleTransaction;

/**
 * Specialization of LittleTransaction gives access to a
 * transaction-managed entity manager.
 */
public interface JpaLittleTransaction extends LittleTransaction {
    public EntityManager getEntityManager ();

}
