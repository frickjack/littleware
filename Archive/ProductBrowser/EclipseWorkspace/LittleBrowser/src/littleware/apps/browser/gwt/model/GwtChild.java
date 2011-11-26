package littleware.apps.browser.gwt.model;

import java.util.Date;

public interface GwtChild extends GwtAsset {
	public GwtUUID getParentId();
	@Override public GwtChildBuilder copy();
	
	public interface GwtChildBuilder extends GwtAsset.GwtAssetBuilder {
		public GwtUUID  getParentId();
		public void     setParentId( GwtUUID value );
		public GwtChildBuilder  parentId( GwtUUID value );
		/**
		 * Also set aclId, homeId, etc. from parent value ...
		 */
		public GwtChildBuilder  parent( GwtParent value );
		
		@Override public GwtChild         build();
		@Override public GwtChildBuilder  name( String value );
		@Override public GwtChildBuilder  id( GwtUUID id );
		@Override public GwtChildBuilder  homeId( GwtUUID id );
		@Override public GwtChildBuilder  timestamp( long value );
		@Override public GwtChildBuilder  comment( String value );
		@Override public GwtChildBuilder  creatorId( GwtUUID value );
		@Override public GwtChildBuilder  createDate( Date value );
		@Override public GwtChildBuilder  updaterId( GwtUUID value );
		@Override public GwtChildBuilder  updateDate( Date value );
		@Override public GwtChildBuilder  updateComment( String value );
		@Override public GwtChildBuilder  aclId( GwtUUID id );
		@Override public GwtChildBuilder  ownerId( GwtUUID id );
	}
}
