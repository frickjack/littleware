package littleware.asset.server.db.memory;

import java.util.LinkedList;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import littleware.asset.Asset;

public class InMemDb {
    public final ImmutableMap<UUID, Asset> assetIndex = ImmutableMap.of();
    public final ImmutableMap<UUID, ImmutableList<Asset>> assetChildren = ImmutableMap.of();
    public final ImmutableList<Edit>  editLog = ImmutableList.of();
    private long timestamp = 0L;
    private final LinkedList<InMemLittleTransaction> activeTransList = new LinkedList<>();


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
    public synchronized long startTransaction(InMemLittleTransaction trans) {
        // verify that this transaction has not already registered
        for (var it : this.activeTransList) {
            if (it == trans) { 
                throw new IllegalStateException("transaction already active");
            }
        }
        this.timestamp++;
        final long timestamp = this.timestamp;
        this.activeTransList.add(trans);
        return timestamp;
    }

    synchronized void applyEdits(ImmutableList<Edit> editList) {
        throw new UnsupportedOperationException(); // TODO!
    }

    /**
     * Complete a transaction, and apply its edit list
     * 
     * @exception InterruptedException on shutdown or similar event
     */
    synchronized void endTransaction(InMemLittleTransaction trans, ImmutableList<Edit> editList) throws InterruptedException {
        int index = 0;
        for (var it : this.activeTransList) {
            if (it == trans) {
                break;
            }
            index++;
        }
        if (index >= this.activeTransList.size()) {
            throw new IllegalStateException("attempt to commit unregistered transaction");
        }
        while(this.activeTransList.getFirst() != trans) {
            this.wait();
        }
        this.activeTransList.pop();
        if (! editList.isEmpty()) {
            this.applyEdits(editList);
        }
        if (! this.activeTransList.isEmpty()) {
            this.notifyAll();
        }
    }
}
