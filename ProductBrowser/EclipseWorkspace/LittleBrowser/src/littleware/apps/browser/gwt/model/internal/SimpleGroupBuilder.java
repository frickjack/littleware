package littleware.apps.browser.gwt.model.internal;

import java.util.ArrayList;
import java.util.Collection;

import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtGroup;
import littleware.apps.browser.gwt.model.GwtGroup.GwtGroupBuilder;
import littleware.apps.browser.gwt.model.GwtPrincipal;
import littleware.apps.browser.gwt.model.GwtUUID;
import littleware.apps.browser.gwt.model.GwtUser;

public class SimpleGroupBuilder extends AbstractAssetBuilder<GwtGroup.GwtGroupBuilder> implements GwtGroup.GwtGroupBuilder {
	private static final long serialVersionUID = 1L;

	public SimpleGroupBuilder() {
		super( GwtGroup.GROUP_TYPE );
	}
	
	
	public static class SimpleGroup extends AbstractAsset implements GwtGroup {
		private static final long serialVersionUID = 123L;

		private Collection<GwtUUID> memberIds = new ArrayList<GwtUUID>();
		
		private transient Collection<GwtPrincipal> members = new ArrayList<GwtPrincipal>();
		
		public SimpleGroup() {
		}
		public SimpleGroup( SimpleGroupBuilder builder, Collection<GwtPrincipal> members ) {
			super( builder );
			this.members = new ArrayList<GwtPrincipal>( members );
			for( GwtPrincipal member : members ) {
				memberIds.add( member.getId() );
			}
		}

		@Override
		public GwtGroup.GwtGroupBuilder copy() {
			final SimpleGroupBuilder builder = new SimpleGroupBuilder();
			builder.copy( this );
			return builder;
		}
		@Override
		public Collection<GwtPrincipal> getMembers() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not yet implemented" );
		}
		@Override
		public boolean isMember(GwtUser user) {
			for( GwtPrincipal member : this.members ) {
				if( member.getId().equals( user.getId() ) ) {
					return true;
				} else if ( (member instanceof GwtGroup)
						&& ((GwtGroup) member).isMember( user )
						) {
					return true;
				}
			}
			return false;
		}

	}

	@Override
	public GwtGroup build() {
		return new SimpleGroup( this, members );
	}

	private Collection<GwtPrincipal> members = new ArrayList<GwtPrincipal>();
	
	@Override
	public void addMember(GwtPrincipal member) {
		members.add( member );
	}

}
