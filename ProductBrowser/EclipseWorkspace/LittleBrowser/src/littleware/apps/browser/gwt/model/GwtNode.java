package littleware.apps.browser.gwt.model;

import java.util.Date;


public interface GwtNode extends GwtChild, GwtParent {
	@Override public GwtNodeBuilder copy();
	public static final GwtAssetType TREE_NODE_TYPE = GwtAssetType.build(
			"littleware.GwtNode",
			GwtUUID.fromString("3c766457-d7a7-432b-b6db-281053672204")
			);


	public interface GwtNodeBuilder extends GwtChildBuilder {
		@Override public GwtNode         build();
		@Override public GwtNodeBuilder  parentId( GwtUUID value );
		@Override public GwtNodeBuilder  parent( GwtParent value );
		@Override public GwtNodeBuilder  name( String value );
		@Override public GwtNodeBuilder  id( GwtUUID id );
		@Override public GwtNodeBuilder  homeId( GwtUUID id );
		@Override public GwtNodeBuilder  timestamp( long value );
		@Override public GwtNodeBuilder  comment( String value );
		@Override public GwtNodeBuilder  creatorId( GwtUUID value );
		@Override public GwtNodeBuilder  createDate( Date value );
		@Override public GwtNodeBuilder  updaterId( GwtUUID value );
		@Override public GwtNodeBuilder  updateDate( Date value );
		@Override public GwtNodeBuilder  updateComment( String value );
		@Override public GwtNodeBuilder  aclId( GwtUUID id );
		@Override public GwtNodeBuilder  ownerId( GwtUUID id );

	}
	
	public static class Factory {
		public GwtNodeBuilder get() {
			return new littleware.apps.browser.gwt.model.internal.SimpleTreeNodeBuilder();
		}
	}
}
