package littleware.apps.browser.gwt.model.internal;

import littleware.apps.browser.gwt.model.GwtHome;
import littleware.apps.browser.gwt.model.GwtUUID;

public class SimpleHomeBuilder extends AbstractAssetBuilder<GwtHome.GwtHomeBuilder> implements GwtHome.GwtHomeBuilder {
	private static final long serialVersionUID = 1111L;
	
	public SimpleHomeBuilder() {
		super( GwtHome.HomeType );
	}

	public static class SimpleHome extends AbstractAsset implements GwtHome {

		private static final long serialVersionUID = 1444L;
		public SimpleHome() {}
		public SimpleHome(SimpleHomeBuilder builder ) {
			super( builder );
		}

		@Override
		public GwtHome.GwtHomeBuilder copy() {
			final GwtHomeBuilder builder = new SimpleHomeBuilder();
			builder.copy( this );
			return builder;
		}
		
	}
	
	@Override public GwtHome build() {
		return new SimpleHome( this );
	}

}
