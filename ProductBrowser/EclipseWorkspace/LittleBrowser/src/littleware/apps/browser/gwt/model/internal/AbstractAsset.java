package littleware.apps.browser.gwt.model.internal;

import java.util.Date;

import littleware.apps.browser.gwt.model.GwtAssetType;
import littleware.apps.browser.gwt.model.GwtUUID;


/**
 * GWT friendly adaptation of littleware.asset.Asset
 */
public abstract class AbstractAsset implements java.io.Serializable {
	private static final long serialVersionUID = 1234L;
	
	private String name = "unset";
	private GwtAssetType  assetType = null;
	private GwtUUID id = GwtUUID.randomId();
	private String  comment = "";
	private long    timestamp = 0L;
	private GwtUUID fromId = null;
	private GwtUUID toId = null;
	private GwtUUID creatorId = null;
	private Date    createDate = null;
	private GwtUUID updaterId = null;
	private Date    updateDate = null;
	private String  updateComment = "";
	private GwtUUID aclId = null;
	private GwtUUID ownerId = null;
	private int     state = 0;

	private GwtUUID homeId;
	
	protected AbstractAsset() {}
	protected AbstractAsset( String name, 
			GwtAssetType assetType,
			GwtUUID     id,
			GwtUUID     homeId,
			String      comment,
			long        timestamp,
			GwtUUID     fromId,
			GwtUUID     toId,
			GwtUUID     creatorId,
			Date        createDate,
			GwtUUID     updaterId,
			Date        updateDate,
			String      updateComment,
			GwtUUID     aclId,
			GwtUUID     ownerId,
			int         state
			) 
	{
		this.name = name;
		this.assetType = assetType;
		this.id = id;
		this.homeId = homeId;
		this.comment = comment;
		this.timestamp = timestamp;
		this.fromId = fromId;
		this.toId = toId;
		this.creatorId = creatorId;
		this.createDate = createDate;
		this.updaterId = updaterId;
		this.updateDate = updateDate;
		this.updateComment = updateComment;
		this.aclId = aclId;
		this.ownerId = ownerId;
		this.state = state;
	}
	
	protected AbstractAsset( AbstractAssetBuilder<?> builder ) {
		this( builder.getName(), 
				builder.getAssetType(),
				builder.getId(),
				builder.getHomeId(),
				builder.getComment(),
				builder.getTimestamp(),
				builder.getFromId(),
				builder.getToId(),
				builder.getCreatorId(),
				builder.getCreateDate(),
				builder.getUpdaterId(),
				builder.getUpdateDate(),
				builder.getUpdateComment(),
				builder.getAclId(),
				builder.getOwnerId(),
				builder.getState()
				);
	}
	
	public String getName() { return name; }
	public GwtAssetType getAssetType() { return assetType; }
	public GwtUUID getId() { return id; }
	public GwtUUID  getHomeId() { return homeId; }
	public String getComment() { return comment; }
	public long   getTimestamp() { return timestamp; }
	public GwtUUID  getFromId() { return fromId; }
	public GwtUUID  getParentId() { return getFromId(); }
	public GwtUUID  getToId() { return toId; }
	public GwtUUID  getCreatorId() { return creatorId; }
	public Date     getCreateDate() { return createDate; }
	public GwtUUID  getUpdaterId() { return updaterId; }
	public Date     getUpdateDate() { return updateDate; }
	public String   getUpdateComment() { return updateComment; }
	public GwtUUID  getAclId() { return aclId; }
	public GwtUUID  getOwnerId() { return ownerId; }
	public int      getState() { return state; }
	
}

