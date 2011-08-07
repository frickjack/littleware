package littleware.apps.browser.gwt.model;

import java.util.Date;

public interface TreeChild extends GwtAsset {
	public GwtUUID getParentId();
	@Override public TreeChildBuilder copy();
	
	public interface TreeChildBuilder extends GwtAsset.GwtAssetBuilder {
		public GwtUUID  getParentId();
		public void     setParentId( GwtUUID value );
		public TreeChildBuilder  parentId( GwtUUID value );
		/**
		 * Also set aclId, homeId, etc. from parent value ...
		 */
		public TreeChildBuilder  parent( TreeParent value );
		
		@Override public TreeChild         build();
		@Override public TreeChildBuilder  name( String value );
		@Override public TreeChildBuilder  id( GwtUUID id );
		@Override public TreeChildBuilder  homeId( GwtUUID id );
		@Override public TreeChildBuilder  timestamp( long value );
		@Override public TreeChildBuilder  comment( String value );
		@Override public TreeChildBuilder  creatorId( GwtUUID value );
		@Override public TreeChildBuilder  createDate( Date value );
		@Override public TreeChildBuilder  updaterId( GwtUUID value );
		@Override public TreeChildBuilder  updateDate( Date value );
		@Override public TreeChildBuilder  updateComment( String value );
		@Override public TreeChildBuilder  aclId( GwtUUID id );
		@Override public TreeChildBuilder  ownerId( GwtUUID id );
	}
}
