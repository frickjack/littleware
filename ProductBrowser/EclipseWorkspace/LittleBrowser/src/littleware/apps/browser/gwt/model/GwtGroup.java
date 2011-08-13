package littleware.apps.browser.gwt.model;

import java.util.Collection;
import java.util.Date;



public interface GwtGroup extends GwtNode, GwtPrincipal {
    public static final GwtAssetType GROUP_TYPE = GwtAssetType.build(
    		"littleware.GROUP",
            GwtUUID.fromString("FAA894CEC15B49CF8F8EC5C280062776")
            );
    
    public Collection<GwtPrincipal> getMembers();
    public boolean isMember( GwtUser user );
    
    @Override public GwtGroupBuilder copy();

	public interface GwtGroupBuilder extends GwtNode.GwtNodeBuilder {
		@Override public GwtGroup         build();
		@Override public GwtGroupBuilder  parentId( GwtUUID value );
		@Override public GwtGroupBuilder  parent( GwtParent value );
		@Override public GwtGroupBuilder  name( String value );
		@Override public GwtGroupBuilder  id( GwtUUID id );
		@Override public GwtGroupBuilder  homeId( GwtUUID id );
		@Override public GwtGroupBuilder  timestamp( long value );
		@Override public GwtGroupBuilder  comment( String value );
		@Override public GwtGroupBuilder  creatorId( GwtUUID value );
		@Override public GwtGroupBuilder  createDate( Date value );
		@Override public GwtGroupBuilder  updaterId( GwtUUID value );
		@Override public GwtGroupBuilder  updateDate( Date value );
		@Override public GwtGroupBuilder  updateComment( String value );
		@Override public GwtGroupBuilder  aclId( GwtUUID id );
		@Override public GwtGroupBuilder  ownerId( GwtUUID id );
		
		public void  addMember( GwtPrincipal member );
	}

	public static class Factory {
		public static GwtGroup.GwtGroupBuilder get() {
			return new littleware.apps.browser.gwt.model.internal.SimpleGroupBuilder();
		}
	}
	
}
