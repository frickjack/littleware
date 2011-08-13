package littleware.apps.browser.gwt.controller.internal;

import java.util.Map;

import littleware.apps.browser.gwt.controller.AssetSearchManager;
import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtOption;
import littleware.apps.browser.gwt.model.GwtUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface AssetSearchServiceAsync {
	//public void        getAssetAtPath( String path, AsyncCallback<GwtOption<AssetSearchManager.AssetWithPath>> callback );
	public void        getAsset( GwtUUID id, AsyncCallback<GwtOption<GwtAsset>> callback );
	public void        getHomeIds( AsyncCallback<Map<String,GwtUUID>> callback );
	public void        getAssetsUnder( GwtUUID parentId, AsyncCallback<Map<String,GwtUUID>> callback  );
}
