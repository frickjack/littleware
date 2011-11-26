package littleware.apps.browser.gwt.client.view;


import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasTreeItems.ForIsWidget;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;

import java.util.HashMap;
import java.util.Map;

import littleware.apps.browser.gwt.client.model.SimpleBrowseModel;
import littleware.apps.browser.gwt.controller.BrowserServiceAsync;
import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtUUID;


public class AssetTreeView extends Composite {

	private static AssetTreeViewUiBinder uiBinder = GWT
			.create(AssetTreeViewUiBinder.class);
	
	@UiField
	Tree    assetTree;
	private final BrowserServiceAsync search;
	
	private SimpleBrowseModel  browseModel = new SimpleBrowseModel();

    /*
     * Create the tree using the model. We specify the default value of the
     * hidden root node as "Item 1".
     */
    //private CellTree tree = new CellTree(model, "Item 1");
	

	interface AssetTreeViewUiBinder extends UiBinder<Widget, AssetTreeView> {
	}

	public AssetTreeView( BrowserServiceAsync search ) {
		this.search = search;
		initWidget(uiBinder.createAndBindUi(this));
		assetTree.addItem( new TreeItem( "/" ) );
	}


	public SimpleBrowseModel  getBrowseModel() {
		return browseModel;
	}
	
	private TreeItem  treeItem( GwtAsset asset ) {
		return new TreeItem( asset.getName() + " [" + asset.getAssetType() + "]" );
	}
	
	public void setBrowseModel( SimpleBrowseModel value ) {
		this.browseModel = value;
		final TreeItem root = assetTree.getItem(0);
		root.removeItems();
		final Map<GwtUUID,TreeItem> itemMap = new HashMap<GwtUUID,TreeItem>();
		for( GwtAsset asset : browseModel.getAsset() ) {
			for( GwtAsset parent : browseModel.getParents() ) {
				final TreeItem tiParent = treeItem( parent );
				root.addItem( tiParent );
				if ( parent.getId().equals( asset.getFromId() ) ) {
					for( GwtAsset sib : browseModel.getSiblings() ) {
						final TreeItem tiSib = treeItem( sib );
						tiParent.addItem( tiSib );
						if ( sib.getId().equals( asset.getId() ) ) {
							tiSib.setSelected(true);
							for( GwtAsset child : browseModel.getChildren() ) {
								final TreeItem tiChild = treeItem( child );
								tiSib.addItem( tiChild );
							}
						}
					}
				}
			}
		}
	}
}
