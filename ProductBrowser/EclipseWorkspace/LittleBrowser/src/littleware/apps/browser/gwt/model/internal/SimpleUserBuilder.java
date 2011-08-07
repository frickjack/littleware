package littleware.apps.browser.gwt.model.internal;

import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtUser;
import littleware.apps.browser.gwt.model.GwtUser.GwtUserBuilder;
import littleware.apps.browser.gwt.model.GwtUser.Status;

public class SimpleUserBuilder extends AbstractAssetBuilder<GwtUser.GwtUserBuilder> implements GwtUser.GwtUserBuilder {
	private static final long serialVersionUID = 1L;

	public SimpleUserBuilder() {
		super( GwtUser.USER_TYPE );
	}
	
	@Override
	public final Status getStatus() {
		if ( 0 == getState() ) {
			return GwtUser.Status.ACTIVE;
		} else {
			return GwtUser.Status.INACTIVE;
		}
	}

	@Override
	public final void setStatus(Status value) {
		if ( value.equals( GwtUser.Status.ACTIVE )) {
			setState( 0 );
		} else {
			setState( 1 );
		}
	}

	@Override
	public GwtUserBuilder status(Status value) {
		setStatus( value );
		return this;
	}
	
	public static class SimpleUser extends AbstractAsset implements GwtUser {
		private static final long serialVersionUID = 123L;

		public SimpleUser() {
		}
		public SimpleUser( SimpleUserBuilder builder ) {
			super( builder );
		}

		@Override
		public GwtUser.GwtUserBuilder copy() {
			final SimpleUserBuilder builder = new SimpleUserBuilder();
			builder.copy( this );
			return builder;
		}

		@Override
		public Status getStatus() {
			if ( 0 == getState() ) {
				return GwtUser.Status.ACTIVE;
			} else { 
				return GwtUser.Status.INACTIVE;
			}
		}
	}

	@Override
	public GwtUser build() {
		return new SimpleUser( this );
	}

}
