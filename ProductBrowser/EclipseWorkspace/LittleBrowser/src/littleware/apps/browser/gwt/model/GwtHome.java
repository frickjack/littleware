package littleware.apps.browser.gwt.model;

import java.util.Date;



public interface GwtHome extends GwtAsset, GwtParent {
	public static GwtAssetType HomeType = GwtAssetType.build("littleware.home", GwtUUID.fromString("BD46E5588F9D4F41A6310100FE68DCB4") );
	public static GwtUUID      littleHomeId = GwtUUID.fromString( "BD46E5588F9D4F41A6310100FE68DCB4" );
	
	
	public interface GwtHomeBuilder extends GwtAsset.GwtAssetBuilder {
		@Override public GwtHome         build();
		@Override public GwtHomeBuilder  name( String value );
		@Override public GwtHomeBuilder  id( GwtUUID id );
		@Override public GwtHomeBuilder  homeId( GwtUUID id );
		@Override public GwtHomeBuilder  timestamp( long value );
		@Override public GwtHomeBuilder  comment( String value );
		@Override public GwtHomeBuilder  creatorId( GwtUUID value );
		@Override public GwtHomeBuilder  createDate( Date value );
		@Override public GwtHomeBuilder  updaterId( GwtUUID value );
		@Override public GwtHomeBuilder  updateDate( Date value );
		@Override public GwtHomeBuilder  updateComment( String value );
		@Override public GwtHomeBuilder  aclId( GwtUUID id );
		@Override public GwtHomeBuilder  ownerId( GwtUUID id );
		
	}
	
	public static class Factory {
		public static GwtHome.GwtHomeBuilder get() {
			return new littleware.apps.browser.gwt.model.internal.SimpleHomeBuilder();
		}
	}
}
