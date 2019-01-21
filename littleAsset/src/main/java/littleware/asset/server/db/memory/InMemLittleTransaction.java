package littleware.asset.server.db.memory;

import javax.persistence.EntityManager;

import com.google.common.collect.ImmutableList;

import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.AbstractLittleTransaction;

/**
 * Specialization of LittleTransaction gives access to a
 * transaction-managed entity manager.
 */
public class InMemLittleTransaction extends AbstractLittleTransaction {
    private final InMemDb db;
    private long timestamp = -1L;
    private ImmutableList.Builder<InMemDb.Edit> edListBuilder = ImmutableList.builder();

    public InMemLittleTransaction(InMemDb db) {
        this.db = db;
    }
    
    public void pushEdit(InMemDb.Edit edit) {

    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    protected void endDbAccess(int levelNumber) {
    }

    @Override
    public void startDbUpdate() {
        if (!isDbUpdating()) {
            this.timestamp = this.db.startTransaction(this);
        }
        super.startDbUpdate();
    }

    @Override
    protected void endDbUpdate(boolean rollback, int updateLevel) {
        if (updateLevel == 0) {
            final ImmutableList<InMemDb.Edit> editList = this.edListBuilder.build();
            this.edListBuilder  = ImmutableList.builder();
            this.db.endTransaction(this, editList);
            this.timestamp = -1L;
        }
    }

}
