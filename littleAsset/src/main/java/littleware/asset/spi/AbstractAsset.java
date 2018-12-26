package littleware.asset.spi;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.cache.AbstractCacheableObject;
import littleware.base.validate.ValidationException;

/**
 * Handy little base class for implementing asset types.
 * Subtypes actually implement the Asset interface.
 */
public abstract class AbstractAsset extends AbstractCacheableObject {
    private static final Logger log = Logger.getLogger( AbstractAsset.class.getName() );
    
    private final UUID homeId;
    private final UUID ownerId;
    private final UUID fromId;
    private final UUID toId;
    private final UUID aclId;
    private final String name;
    private final int state;
    private final Date createDate;
    private final Date endDate;
    private final Date startDate;
    private final UUID creatorId;
    private final String comment;
    private final Date lastUpdateDate;
    private final UUID lastUpdaterId;
    private final String lastUpdate;
    private final AssetType type;
    private final float value;
    private final String data;
    private final ImmutableMap<String, String> attributeMap;
    private final ImmutableMap<String, Date> dateMap;
    private final ImmutableMap<String, UUID> linkMap;

    
    public AbstractAsset(AssetType type, UUID id, UUID homeId, UUID ownerId, UUID fromId, UUID toId, UUID aclId, long timestamp, String name, Integer state, Date createDate, UUID creatorId, String comment, Date lastUpdateDate, UUID lastUpdaterId, String lastUpdate, Date startDate, Date endDate, Float value, String data, ImmutableMap<String, String> attributeMap, ImmutableMap<String, Date> dateMap, ImmutableMap<String, UUID> linkMap) {
        super(id, timestamp);
        this.type = type;
        this.homeId = homeId;
        this.ownerId = ownerId;
        this.fromId = fromId;
        this.toId = toId;
        this.aclId = aclId;
        this.name = name;
        this.state = state;
        this.createDate = createDate;
        this.creatorId = creatorId;
        this.comment = comment;
        this.lastUpdateDate = lastUpdateDate;
        this.lastUpdaterId = lastUpdaterId;
        this.lastUpdate = lastUpdate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.value = value;
        this.data = data;
        this.dateMap = dateMap;
        this.attributeMap = attributeMap;
        this.linkMap = linkMap;
        
        try {
            ValidationException.validate( null != linkMap, "What's up with the null linkMap?");
            ValidationException.validate( null != attributeMap, "What's up with the null attribteMap?");
            ValidationException.validate( null != dateMap, "What's up with the null dateMap?");
        } catch ( RuntimeException ex ) {
            log.log( Level.WARNING, "What the frick?", ex );
            throw ex;
        }
    }

    public AbstractAsset( AbstractAssetBuilder<? extends AssetBuilder> builder) {
        this(builder.getAssetType(), builder.getId(), builder.getHomeId(), 
            builder.getOwnerId(), builder.getFromId(), builder.getToId(), 
            builder.getAclId(), builder.getTimestamp(), builder.getName(), 
            builder.getState(), builder.getCreateDate(), builder.getCreatorId(), 
            builder.getComment(), builder.getLastUpdateDate(), builder.getLastUpdaterId(), 
            builder.getLastUpdate(), builder.getStartDate(), builder.getEndDate(), 
            builder.getValue(), builder.getData(), 
            ImmutableMap.copyOf(builder.getAttributeMap()), 
            ImmutableMap.copyOf(builder.getDateMap()), 
            ImmutableMap.copyOf(builder.getLinkMap())
            );
    }

    
    public UUID getHomeId() {
        return homeId;
    }

    /**
     * @return the ownerId
     */
    public UUID getOwnerId() {
        return ownerId;
    }

    /**
     * @return the fromId
     */
    public UUID getFromId() {
        return fromId;
    }

    /**
     * @return getFromId
     */
    public UUID getParentId() {
        return getFromId();
    }


    /**
     * @return the toId
     */
    public UUID getToId() {
        return toId;
    }

    /**
     * @return the aclId
     */
    public UUID getAclId() {
        return aclId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the state
     */
    public Integer getState() {
        return state;
    }

    /**
     * @return the createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @return the creatorId
     */
    public UUID getCreatorId() {
        return creatorId;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the lastUpdateDate
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @return the lastUpdaterId
     */
    public UUID getLastUpdaterId() {
        return lastUpdaterId;
    }

    /**
     * @return the lastUpdate
     */
    public String getLastUpdate() {
        return lastUpdate;
    }

    public AssetType getAssetType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public Float getValue() {
        return value;
    }

    public <T extends Asset> T narrow(Class<T> type) {
        return type.cast(this);
    }

    public <T extends Asset> T narrow() {
        return (T) this;
    }

    public abstract AssetBuilder copy();

    public String getStateString() {
        return getState().toString();
    }

    public Map<String, UUID> getLinkMap() {
        return linkMap;
    }

    public Optional<UUID> getLink(String key) {
        return Optional.ofNullable(linkMap.get(key));
    }

    public Map<String, Date> getDateMap() {
        return dateMap;
    }

    public Optional<Date> getDate(String key) {
        return Optional.ofNullable(dateMap.get(key));
    }

    public ImmutableMap<String, String> getAttributeMap() {
        return attributeMap;
    }

    public Optional<String> getAttribute(String key) {
        return Optional.ofNullable(attributeMap.get(key));
    }

    @Override
    public String toString() {
        return "Asset " + this.getAssetType() + " " + this.getName() + " (" + this.getId() + ")";
    }
}
