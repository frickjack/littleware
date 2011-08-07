package littleware.apps.browser.gwt.model.internal;

import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.TreeNode;


public class SimpleTreeNodeBuilder extends AbstractAssetBuilder<TreeNode.TreeNodeBuilder> implements TreeNode.TreeNodeBuilder {
	public SimpleTreeNodeBuilder() {
		super( TreeNode.TREE_NODE_TYPE );
	}
	
	public static class SimpleNode extends AbstractAsset implements TreeNode {

		public SimpleNode() {}
		public SimpleNode( SimpleTreeNodeBuilder builder ) {
			super( builder );
		}
		
		@Override
		public TreeNodeBuilder copy() {
			final SimpleTreeNodeBuilder builder = new SimpleTreeNodeBuilder();
			builder.copy( this );
			return builder;
		}
		
	}

	@Override
	public TreeNode build() {
		return new SimpleNode( this );
	}
}
