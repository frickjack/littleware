package littleware.asset;

import java.util.Optional;
import java.util.UUID;


/**
 * Little POJO returned by AssetSearchManager.checkTransactionLog
 * to help a client keep its cache in sync with the server.
 * Tracks asset id with a transaction count and parent/from id.
 */
public interface IdWithClock {

    /** Factory for IdWithClock objects */
    public interface Builder {
        public IdWithClock build( UUID id, long timestamp );
        /** from may be null */
        public IdWithClock build( UUID id, UUID parent, long timestamp );
    }


    public UUID getId();
    /**
     * From property set if asset with id has a non-null fromId.
     * A client that is not tracking id may still chose to load
     * id if the client is tracking id's parent.
     */
    public Optional<UUID> getParentId();
    public long getTimestamp();
}
