/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.browser.gwt.model;

import java.util.Date;

public interface GwtAsset {
	public String getName();
	public GwtAssetType  getAssetType();
	public GwtUUID       getId();
	public GwtUUID       getHomeId();
	public String        getComment();
	public long          getTimestamp();
	public GwtUUID       getCreatorId();
	public Date          getCreateDate();
	public GwtUUID       getUpdaterId();
	public Date          getUpdateDate();
	public String        getUpdateComment();
	public GwtUUID       getAclId();
	public GwtUUID       getOwnerId();
    public GwtAssetBuilder copy();
	
	
	public interface GwtAssetBuilder {
		public String getName();
		public void setName( String value );
		public GwtAssetBuilder name( String value );
		
		public GwtUUID  getId();
		public void     setId( GwtUUID value );
		public GwtAssetBuilder id( GwtUUID value );
		
		public GwtUUID  getHomeId();
		public void     setHomeId( GwtUUID value );
		public GwtAssetBuilder  homeId( GwtUUID value );
		
		public String   getComment();
		public void     setComment( String value );
		public GwtAssetBuilder comment( String value );
		
		public long     getTimestamp();
		public void     setTimestamp( long value );
		public GwtAssetBuilder  timestamp( long value );

		public GwtUUID          getCreatorId();
		public void             setCreatorId( GwtUUID value );
		public GwtAssetBuilder  creatorId( GwtUUID value );
		
		public Date             getCreateDate();
		public void             setCreateDate( Date value );
		public GwtAssetBuilder  createDate( Date value );

		public GwtUUID          getUpdaterId();
		public void             setUpdaterId( GwtUUID value );
		public GwtAssetBuilder  updaterId( GwtUUID value );
		
		public Date             getUpdateDate();
		public void             setUpdateDate( Date value );
		public GwtAssetBuilder  updateDate( Date value );

		public String   getUpdateComment();
		public void     setUpdateComment( String value );
		public GwtAssetBuilder updateComment( String value );
		
		public GwtAssetType getAssetType();
		
		public GwtUUID       getAclId();
		public void          setAclId( GwtUUID value );
		public GwtAssetBuilder aclId( GwtUUID value );
		
		public GwtUUID       getOwnerId();
		public void          setOwnerId( GwtUUID value );
		public GwtAssetBuilder ownerId( GwtUUID value );
		
		public GwtAssetBuilder copy(GwtAsset value);
		public GwtAsset build();
	}
}