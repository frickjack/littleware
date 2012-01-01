package littleware.apps.browser.gwt.server.internal;

import java.util.HashMap;
import java.util.Map;

import littleware.apps.browser.gwt.controller.BrowserService;
import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtHome;
import littleware.apps.browser.gwt.model.GwtNode;
import littleware.apps.browser.gwt.model.GwtOption;
import littleware.apps.browser.gwt.model.GwtUUID;
import littleware.apps.browser.gwt.model.SimpleBrowserModel;

/**
 * Mock implementation of search service
 */
public class MockBrowserService implements BrowserService {
	private Map<GwtUUID,GwtAsset>  assetCache = new HashMap<GwtUUID,GwtAsset>();

	{ // just setup a bogus little repository
		assetCache.put( GwtHome.littleHomeId,
				GwtHome.Factory.get().id( GwtHome.littleHomeId 
						).name( "littleware.home"
								).comment( "littleware home" 
										).build()
				);
		for( int i=1; i < 4; ++i ) {
			final GwtNode folder = GwtNode.Factory.get().homeId(
					GwtHome.littleHomeId
					).parentId( 
							GwtHome.littleHomeId 
							).id( GwtUUID.fromString( "folder" + i )
									).name( "folder" + i
											).build();
			assetCache.put( folder.getId(), folder );
			
			for( int j=1; j < 4; ++j ) {
				final GwtNode child = GwtNode.Factory.get().homeId(
						GwtHome.littleHomeId
						).parentId( 
							folder.getId()
						).id( GwtUUID.fromString( folder.getName() + "c" + j )
						).name( "child" + j
						).build();
				assetCache.put( child.getId(), child);
			}
		}
	}
	

	@Override
	public GwtOption<GwtAsset> getAsset(GwtUUID id) {
		// TODO Auto-generated method stub
		return GwtOption.Factory.some( assetCache.get( id ) );
	}

	@Override
	public Map<String, GwtUUID> getHomeIds() {
		// TODO Auto-generated method stub
		final GwtAsset home = assetCache.get( GwtHome.littleHomeId );
		final Map<String,GwtUUID> result = new HashMap<String,GwtUUID>();
		result.put( home.getName(), home.getId() );
		return result;
	}

	@Override
	public Map<String, GwtUUID> getAssetsUnder(GwtUUID parentId) {
		final Map<String,GwtUUID> result = new HashMap<String,GwtUUID>();
		for( GwtAsset asset : assetCache.values() ) {
			if ( asset.getFromId().equals( parentId ) ) {
				result.put( asset.getName(), asset.getId() );
			}
		}
		return result;
	}

	@Override
	public SimpleBrowserModel loadBrowserModel(GwtUUID assetId) {
		for( GwtAsset asset : getAsset( assetId ) ) {
			
		}
		return null;
	}

}
