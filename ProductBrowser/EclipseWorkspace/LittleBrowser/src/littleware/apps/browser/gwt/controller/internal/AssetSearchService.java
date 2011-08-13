package littleware.apps.browser.gwt.controller.internal;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import littleware.apps.browser.gwt.controller.AssetSearchManager;
import littleware.apps.browser.gwt.model.*;

@RemoteServiceRelativePath("search")
public interface AssetSearchService extends RemoteService {
	//public GwtOption<AssetSearchManager.AssetWithPath>    getAssetAtPath( String path );
	public GwtOption<GwtAsset>         getAsset( GwtUUID id );
	public Map<String,GwtUUID>         getHomeIds(); 
	public Map<String,GwtUUID>         getAssetsUnder( GwtUUID parentId );
}
