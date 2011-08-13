package littleware.apps.browser.gwt.controller;

import java.util.Map;


import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtOption;
import littleware.apps.browser.gwt.model.GwtUUID;

/**
 * Client-facing search API - implementation provides cacheing
 * or whatever - service RMI API is buried
 * internally.
 */
public interface AssetSearchManager {
	public interface AssetWithPath extends java.io.Serializable {
		public GwtAsset getAsset();
		public String   getPath();
	}
	
	public GwtOption<AssetWithPath>    getAssetAtPath( String path );
	public GwtOption<GwtAsset>         getAsset( GwtUUID id );
	public Map<String,GwtUUID>         getHomeIds(); 
	public Map<String,GwtUUID>         getAssetsUnder( GwtUUID parentId );
}
