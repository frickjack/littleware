package littleware.apps.browser.gwt.controller;

import java.util.Map;

import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtOption;
import littleware.apps.browser.gwt.model.GwtUUID;
import littleware.apps.browser.gwt.model.SimpleBrowserModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface BrowserServiceAsync {
	//public void        getAssetAtPath( String path, AsyncCallback<GwtOption<AssetSearchManager.AssetWithPath>> callback );
	public void        getAsset( GwtUUID id, AsyncCallback<GwtOption<GwtAsset>> callback );
	public void        getHomeIds( AsyncCallback<Map<String,GwtUUID>> callback );
	public void        getAssetsUnder( GwtUUID parentId, AsyncCallback<Map<String,GwtUUID>> callback  );
	void loadBrowserModel(GwtUUID assetId,
			AsyncCallback<SimpleBrowserModel> callback);
}
