package littleware.apps.browser.gwt.model.internal;


import java.util.Date;

import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.TreeParent;
import littleware.apps.browser.gwt.model.GwtAssetType;
import littleware.apps.browser.gwt.model.GwtUUID;
import littleware.apps.browser.gwt.model.GwtAsset.GwtAssetBuilder;


public abstract class AbstractAssetBuilder<B extends GwtAsset.GwtAssetBuilder> implements java.io.Serializable {
	private static final long serialVersionUID = 19999L;

	private final GwtAssetType assetType;

	protected AbstractAssetBuilder( GwtAssetType assetType ) {
		this.assetType = assetType;
	}

	public final GwtAssetType  getAssetType() {
		return assetType;
	}

	private String name = "name";
	public final String getName() { return name; }
	public final void setName( String value ) { name = value; }
	public B name( String value ) {
		setName( value );
		return (B) this;
	}

	private GwtUUID id = GwtUUID.randomId();
	public final GwtUUID getId() { return id; }
	public final void setId( GwtUUID value ) {
		id = value;
	}
	public B id( GwtUUID value ) {
		setId( value );
		return (B) this;
	}

	private GwtUUID  homeId = null;
	public final GwtUUID getHomeId() { return homeId; }
	public final void setHomeId( GwtUUID value ) { homeId = value; }
	public B homeId( GwtUUID value ) {
		setHomeId( value );
		return (B) this;
	}


	private String  comment = "";
	public final String   getComment() {
		return comment;
	}
	public final void     setComment( String value ) {
		comment = value;
	}
	public B comment( String value ) {
		setComment( value );
		return (B) this;
	}


	private long  timestamp = 0L;
	public long     getTimestamp() {
		return timestamp;
	}
	public void     setTimestamp( long value ) {
		timestamp = value;
	}
	public B  timestamp( long value ) {
		setTimestamp( value );
		return (B) this;
	}


	private GwtUUID  fromId = null;
	public final GwtUUID getFromId() { return fromId; }
	public final void    setFromId( GwtUUID value ) {
		fromId = value;
	}
	public B fromId( GwtUUID value ) {
		setFromId( value );
		return (B) this;
	}

	public final GwtUUID getParentId() { return getFromId(); }
	public final void setParentId( GwtUUID value ) { setFromId( value ); }
	public final B parentId( GwtUUID value ) { return fromId( value ); }

	private GwtUUID  toId = null;
	public final GwtUUID getToId() { return toId; }
	public final void    setToId( GwtUUID value ) {
		toId = value;
	}
	public B toId( GwtUUID value ) {
		setToId( value );
		return (B) this;
	}


	private GwtUUID creatorId = null;
	public final GwtUUID          getCreatorId() { return creatorId; }
	public final void             setCreatorId( GwtUUID value ) { creatorId = value; }
	public B  creatorId( GwtUUID value ) {
		setCreatorId( value );
		return (B) this;
	}

	private Date createDate = new Date();
	public final Date             getCreateDate() { return createDate; }
	public final void             setCreateDate( Date value ) {
		createDate = value;
	}
	public B  createDate( Date value ) {
		setCreateDate( value );
		return (B) this;
	}

	private GwtUUID  updaterId = null;
	public final GwtUUID          getUpdaterId() {
		return updaterId;
	}
	public final void             setUpdaterId( GwtUUID value ) {
		updaterId = value;
	}
	public B  updaterId( GwtUUID value ) {
		setUpdaterId( value );
		return (B) this;
	}

	private Date updateDate = new Date();
	public final Date             getUpdateDate() {
		return updateDate;
	}
	public final void             setUpdateDate( Date value ) {
		updateDate = value;
	}
	public B  updateDate( Date value ) {
		setUpdateDate( value );
		return (B) this;
	}

	private String updateComment = "";
	public final String   getUpdateComment() { return updateComment; }
	public final void     setUpdateComment( String value ) {
		updateComment = value;
	}
	public B updateComment( String value ) { 
		setUpdateComment( value );
		return (B) this;
	}

	private GwtUUID     aclId = null;
	public final GwtUUID       getAclId() { return aclId; }
	public final void          setAclId( GwtUUID value ) { aclId = value; }
	public B aclId( GwtUUID value ) {
		setAclId( value );
		return (B) this;
	}

	private GwtUUID ownerId = null;
	public final GwtUUID       getOwnerId() { return ownerId; }
	public final void          setOwnerId( GwtUUID value ) { ownerId = value; }
	public B ownerId( GwtUUID value ) {
		setOwnerId( value );
		return (B) this;
	}

	
    protected B parentInternal( GwtAsset parent ) {
        fromId(parent.getId()).homeId(parent.getHomeId()).aclId(parent.getAclId());
        return (B) this;
    }

    public B parent(TreeParent parent) {
        return parentInternal( parent );
    }
	
	

	public abstract GwtAsset build();

	
	/**
	 * Return this cast to GwtAsset.GwtAssetBuilder - utility for
	 * subtypes of AbstractAsset.
	 */
	protected B genericCopy( AbstractAsset source) {
		if ((!source.getAssetType().equals(getAssetType())) ) {
			throw new IllegalArgumentException("Asset type mismatch - " + getAssetType() + " builder cannot copy " + source.getAssetType());
		}

		setId(source.getId());
		setName(source.getName());
		setComment(source.getComment());
		setTimestamp(source.getTimestamp());
		setFromId( getFromId() );
		setToId( getToId() );

		setCreatorId(source.getCreatorId());
		setUpdaterId(source.getUpdaterId());
		setAclId(source.getAclId());
		setUpdateDate(source.getUpdateDate());
		setCreateDate( source.getCreateDate() );
		setUpdateComment( source.getUpdateComment() );
		setOwnerId(source.getOwnerId());
		setHomeId(source.getHomeId());

		//setData(source.getData());

		setFromId(source.getFromId());
		setToId(source.getToId());

		/*
	        setStartDate(source.getStartDate());
	        setEndDate(source.getEndDate());
	        copyMap(source.getLinkMap(), linkMap);
	        copyMap(source.getDateMap(), dateMap);
	        copyMap(source.getAttributeMap(), attributeMap);
		 */
		return (B) this;
	}

	public B copy( GwtAsset value ) {
		return (B) genericCopy( (AbstractAsset) value );
	}

}
