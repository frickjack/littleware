package littleware.asset.db.memory;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import littleware.asset.Asset;

public class SimpleMemDb implements InMemDb {
    private static final Logger log = Logger.getLogger(SimpleMemDb.class.getName());

    // assetIndex and assetChildren collected from last snapshot
    private final ImmutableMap<UUID, Asset> assetIndex = ImmutableMap.of();
    private final ImmutableMap<UUID, ImmutableList<Asset>> assetChildren = ImmutableMap.of();
    // edits since last snapshot
    private ImmutableMap<UUID, Edit>  editsById = ImmutableMap.of();
    private ImmutableMap<UUID, ImmutableList<Asset>> assetChildrenEdits = ImmutableMap.of();
    private final ImmutableList<Edit>  editLog = ImmutableList.of();
    private long timestamp = 0L;
    private long lastSnapshotTime = 0L;
    private final LinkedList<TransInfo> activeTransList = new LinkedList<>();

    private final int maxSnapshotEdits = 10000;
    private final int maxSnapshotPeriodMins = 5;
    private final int transMaxSecs = 30;

    static class TransInfo {
        public final InMemTransaction trans;
        public final long startTimeMs = new Date().getTime();
        public final long timestamp;
        
        public TransInfo(InMemTransaction trans, long timestamp) {
            this.trans = trans;
            this.timestamp = timestamp;
        }
    }
    /**
     * Start a db-update transaction - acquires the next timestamp
     * @param trans
     * @return timestamp for transaction
     */
    public synchronized long startTransaction(InMemTransaction trans) {
        // verify that this transaction has not already registered
        for (var it : this.activeTransList) {
            if (it.trans == trans) { 
                throw new IllegalStateException("transaction already active");
            }
        }

        if (this.activeTransList.size() > 100) {
            // TODO - add metrics tracking
            throw new IllegalStateException("throttling transactions - 100 pending transactions");
        }
        this.timestamp++;
        final long timestamp = this.timestamp;
        this.activeTransList.add(new TransInfo(trans, this.timestamp));
        return timestamp;
    }

    public boolean isTimeForSnapshot() {
        return false;
    }

    synchronized void takeSnapshot() {

    }

    public synchronized boolean takeSnapshotIfTime() {
        return false;
    }

    public synchronized void applyEdits(ImmutableList<Edit> editList) {
        throw new UnsupportedOperationException(); // TODO!
    }

    /**
     * Complete a transaction.
     * 
     * - assign the transaction a completion timestamp
     * - validate that the transaction does not update
     *     any asset that was already updated by another transaction
     *     running in parallel 
     * - copy each asset update, and assign it the completion timestamp
     * - update the various lookup indexes
     * - update the commit log
     * 
     * @exception InterruptedException on shutdown or similar event
     */
    public synchronized void endTransaction(InMemTransaction trans, ImmutableList<Edit> editList) throws InterruptedException {
        final TransInfo info;
        { // sanity check
            int index = 0;
            for (var it : this.activeTransList) {
                if (it.trans == trans) {
                    break;
                }
                index++;
            }
            if (index >= this.activeTransList.size()) {
                throw new IllegalStateException("attempt to commit unregistered transaction");
            }
            info = this.activeTransList.remove(index);
        }
        //
        // This transaction needs to wait its turn - we
        // We want to apply transactions in the order they start,
        // but that makes the whole pipeline vulnerable to
        // a transaction that hangs or fails in an unexpected way,
        // so we throw out transactions older than transMaxSecs seconds.
        //
        final long transMaxMs = transMaxSecs*1000;
        while(!this.activeTransList.isEmpty() && 
                this.activeTransList.getFirst().trans != trans
                ) {
            final long  now = new Date().getTime();
            boolean isListChanged = false;
            for( TransInfo first = this.activeTransList.getFirst();
                first.trans != trans && now - transMaxMs > first.startTimeMs;
                first = this.activeTransList.getFirst()
                ) {
                log.log(Level.WARNING, "Ejecting transaction that ran for more than " + transMaxSecs + " seconds");
                this.activeTransList.removeFirst();
                isListChanged = true;
            } 
            if(!isListChanged) {
                this.wait();
            }
        }
        this.activeTransList.pop();
        if (! editList.isEmpty()) {
            this.applyEdits(editList);
        }
        if (! this.activeTransList.isEmpty()) {
            this.notifyAll();
        }
    }


    /** */
}
