package littleware.apps.browser.gwt.controller;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import littleware.apps.browser.gwt.model.*;

@RemoteServiceRelativePath("search")
public interface BrowserService extends RemoteService {
	//public GwtOption<AssetSearchManager.AssetWithPath>    getAssetAtPath( String path );
	public GwtOption<GwtAsset>         getAsset( GwtUUID id );
	public Map<String,GwtUUID>         getHomeIds(); 
	public Map<String,GwtUUID>         getAssetsUnder( GwtUUID parentId );
}
