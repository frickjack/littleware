package littleware.apps.browser.gwt.model;

import java.util.Date;



public interface GwtUser extends GwtNode {
    public static final GwtAssetType USER_TYPE = GwtAssetType.build(
    		"littleware.USER",
    		GwtUUID.fromString("2FAFD5D1074F4BF8A4F01753DBFF4CD5")
            );
    /**
     * Little principal-status class
     */
    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public Status getStatus();
    @Override GwtUserBuilder copy();
    
    public interface GwtUserBuilder extends GwtNode.GwtNodeBuilder {
    	public Status  getStatus();
    	public void    setStatus( Status value );
    	public GwtUserBuilder  status( Status value );
    	
		@Override public GwtUser         build();
		@Override public GwtUserBuilder  parentId( GwtUUID value );
		@Override public GwtUserBuilder  parent( GwtParent value );
		@Override public GwtUserBuilder  name( String value );
		@Override public GwtUserBuilder  id( GwtUUID id );
		@Override public GwtUserBuilder  homeId( GwtUUID id );
		@Override public GwtUserBuilder  timestamp( long value );
		@Override public GwtUserBuilder  comment( String value );
		@Override public GwtUserBuilder  creatorId( GwtUUID value );
		@Override public GwtUserBuilder  createDate( Date value );
		@Override public GwtUserBuilder  updaterId( GwtUUID value );
		@Override public GwtUserBuilder  updateDate( Date value );
		@Override public GwtUserBuilder  updateComment( String value );
		@Override public GwtUserBuilder  aclId( GwtUUID id );
		@Override public GwtUserBuilder  ownerId( GwtUUID id );
    	
    }
    
    public static class Factory {
    	public static GwtUserBuilder get() {
    		return new littleware.apps.browser.gwt.model.internal.SimpleUserBuilder();
    	}
    }
}
