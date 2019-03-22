package littleware.asset;

import java.util.UUID;

/**
 * Interface implemented by db-access managers -
 * provides methods to mark begin/end of db access
 * and updates, and sharing a db session.
 */
public interface LittleTransaction {
    public ImmutableList<LittleAsset> getEditList();   
    
    /**
     * Get the transaction-count at the beginning of the current transaction
     *
     * @return transaction-counter to assign to every asset's transaction property
     *             saved during the current transacton
     * @throws IllegalStateException if not in an update transaction
     */
    public long getTimestamp();
}
