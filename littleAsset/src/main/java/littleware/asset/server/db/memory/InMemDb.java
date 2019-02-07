package littleware.asset.server.db.memory;

import com.google.common.collect.ImmutableList;
import littleware.asset.Asset;


public interface InMemDb {
    
    public static enum EditType {
        CREATE, UPDATE, DELETE;
    }

    /**
     * Edit log entry
     */
    static class Edit {
        public final long     timestamp;
        public final EditType editType;
        public final Asset    asset;

        public Edit(long timestamp, EditType editType, Asset asset) {
            this.timestamp = timestamp;
            this.editType = editType;
            this.asset = asset;
        }
    }

    /**
     * Start a db-update transaction - acquires the next timestamp
     * @param trans
     * @return timestamp for transaction
     */
    long startTransaction(InMemTransaction trans);

    /**
     * Frequency of snapshots depends upon the length of
     * the committed command list and the time that has
     * passed since the last snapshot
     */
    boolean isTimeForSnapshot();
    /**
     * Return true if snapshot saved
     */
    boolean takeSnapshotIfTime();

    void applyEdits(ImmutableList<Edit> editList);

    /**
     * Complete a transaction, and apply its edit list
     * 
     * @exception InterruptedException on shutdown or similar event
     */
    void endTransaction(InMemTransaction trans, ImmutableList<Edit> editList) throws InterruptedException;
}
