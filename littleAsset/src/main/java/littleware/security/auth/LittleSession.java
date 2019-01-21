package littleware.security.auth;

import java.util.Date;
import java.util.UUID;

import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;

/**
 * Specialization of Asset for session-tracking.
 * The user the session is associated with is the session creator.
 */
public interface LittleSession extends Asset {

    /**
     * Is this a read-only user session (0 != getValue()) ?
     */
    public boolean isReadOnly();
    /**
     * When the session expires
     */
    public Date getEndDate();


    @Override
    public Builder copy();



    /** SESSION asset type */
    public static final AssetType SESSION_TYPE = new AssetType(
            UUIDFactory.parseUUID("7AC8C92F30C14AD89FA82DB0060E70C2"),
            "littleware.SESSION");

    //----------------------------------------------------------------

    public interface Builder extends AssetBuilder {

        /**
         * Mark this session read-only (setValue(1)) - must save() this
         * session asset for the change to take effect.
         */
        public void setReadOnly(boolean value);
        public Builder readOnly( boolean value );
        public boolean isReadOnly();

        public Date getEndDate();
        public void setEndDate( Date value );
        public Builder endDate( Date value );


        @Override
        public Builder copy(Asset source);

        @Override
        public LittleSession build();

        @Override
        public Builder id(UUID value);

        @Override
        public Builder name(String value);

        @Override
        public Builder creatorId(UUID value);

        @Override
        public Builder lastUpdaterId(UUID value);

        @Override
        public Builder aclId(UUID value);

        @Override
        public Builder ownerId(UUID value);

        @Override
        public Builder lastUpdate(String value);

        @Override
        public Builder homeId(UUID value);

        @Override
        public Builder timestamp(long value);

        @Override
        public Builder comment( String value );

    }
}

