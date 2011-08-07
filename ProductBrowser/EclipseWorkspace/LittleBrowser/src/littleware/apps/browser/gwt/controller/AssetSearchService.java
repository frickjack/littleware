package littleware.apps.browser.gwt.controller;

import java.util.Collection;

import littleware.apps.browser.gwt.model.*;

public interface AssetSearchService {
	public interface AssetWithPath {
		public GwtAsset getAsset();
		public String   getPath();
	}
	
	public AssetWithPath   getAssetAtPath( String path );
	public Collection<GwtUUID>  getAssetsUnder( GwtUUID parentId );
}
