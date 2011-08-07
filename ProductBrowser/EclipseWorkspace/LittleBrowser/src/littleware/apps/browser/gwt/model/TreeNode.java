package littleware.apps.browser.gwt.model;

import java.util.Date;


public interface TreeNode extends TreeChild, TreeParent {
	@Override public TreeNodeBuilder copy();
	public static final GwtAssetType TREE_NODE_TYPE = GwtAssetType.build(
			"littleware.TreeNode",
			GwtUUID.fromString("3c766457-d7a7-432b-b6db-281053672204")
			);



	public interface TreeNodeBuilder extends TreeChildBuilder {
		@Override public TreeNode         build();
		@Override public TreeNodeBuilder  parentId( GwtUUID value );
		@Override public TreeNodeBuilder  parent( TreeParent value );
		@Override public TreeNodeBuilder  name( String value );
		@Override public TreeNodeBuilder  id( GwtUUID id );
		@Override public TreeNodeBuilder  homeId( GwtUUID id );
		@Override public TreeNodeBuilder  timestamp( long value );
		@Override public TreeNodeBuilder  comment( String value );
		@Override public TreeNodeBuilder  creatorId( GwtUUID value );
		@Override public TreeNodeBuilder  createDate( Date value );
		@Override public TreeNodeBuilder  updaterId( GwtUUID value );
		@Override public TreeNodeBuilder  updateDate( Date value );
		@Override public TreeNodeBuilder  updateComment( String value );
		@Override public TreeNodeBuilder  aclId( GwtUUID id );
		@Override public TreeNodeBuilder  ownerId( GwtUUID id );

	}
	
	public static class Factory {
		public TreeNodeBuilder get() {
			return new littleware.apps.browser.gwt.model.internal.SimpleTreeNodeBuilder();
		}
	}
}
