package littleware.apps.browser.gwt.model.internal;

import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtNode;


public class SimpleTreeNodeBuilder extends AbstractAssetBuilder<GwtNode.GwtNodeBuilder> implements GwtNode.GwtNodeBuilder {
	public SimpleTreeNodeBuilder() {
		super( GwtNode.TREE_NODE_TYPE );
	}
	
	public static class SimpleNode extends AbstractAsset implements GwtNode {

		public SimpleNode() {}
		public SimpleNode( SimpleTreeNodeBuilder builder ) {
			super( builder );
		}
		
		@Override
		public GwtNode.GwtNodeBuilder copy() {
			final SimpleTreeNodeBuilder builder = new SimpleTreeNodeBuilder();
			builder.copy( this );
			return builder;
		}
		
	}

	@Override
	public GwtNode build() {
		return new SimpleNode( this );
	}
}
