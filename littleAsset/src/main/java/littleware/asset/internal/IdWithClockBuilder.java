package littleware.asset.internal;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import littleware.asset.IdWithClock;


/**
 * Basic implementation of IdWithClock.Builder
 */
public class IdWithClockBuilder implements IdWithClock.Builder {

    private static class Data implements IdWithClock, Serializable {
        private UUID   id;
        private long   transaction;
        private Optional<UUID> maybeFrom = Optional.empty();


        /** Empty constructor for serialization */
        public Data() {}

        /** Inject read-only properties, from may be null */
        public Data( UUID id, UUID from, long transaction ) {
            this.id = id;
            this.transaction = transaction;
            this.maybeFrom = Optional.ofNullable( from );
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public Optional<UUID> getParentId() {
            return maybeFrom;
        }

        @Override
        public long getTimestamp() {
            return transaction;
        }

    }

    @Override
    public IdWithClock build(UUID id, long transaction) {
        return build( id, null, transaction );
    }

    @Override
    public IdWithClock build(UUID id, UUID from, long transaction) {
        return new Data( id, from, transaction );
    }
}
