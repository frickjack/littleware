package littleware.apps.browser.gwt.client.view;

import java.util.Map;

import littleware.apps.browser.gwt.controller.BrowserServiceAsync;
import littleware.apps.browser.gwt.model.GwtUUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BrowserPanelView extends Composite  {

	public enum ViewState {
		Loading, Active;
	};
	
	private static BrowserPanelViewUiBinder uiBinder = GWT
			.create(BrowserPanelViewUiBinder.class);

	interface BrowserPanelViewUiBinder extends
	UiBinder<Widget, BrowserPanelView> {
	}

	/*
	public BrowserPanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	 */

	@UiField
	Button loadButton;

	@UiField
	public Label topLabel;
	@UiField
	public Label bodyLabel;
	@UiField
	public VerticalPanel sidePanel;

	private final BrowserServiceAsync search;


	public BrowserPanelView( 
			BrowserServiceAsync search,
			AssetTreeView           treeView
			) 
	{
		this.search = search;
		initWidget(uiBinder.createAndBindUi(this));
		loadButton.setText( "Load" );
		sidePanel.add(treeView);
	}

	@UiHandler("loadButton")
	void onClick(ClickEvent e) {
		//Window.alert("Hello!");
		search.getHomeIds(
				new AsyncCallback<Map<String,GwtUUID>>() {
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						bodyLabel.setText( "Ugh!: " + caught );
					}

					public void onSuccess(Map<String,GwtUUID> result) {
						String home = "";
						for( Map.Entry<String,GwtUUID> entry : result.entrySet() ) {
							home += entry.getKey() + "(" + entry.getValue() + "), ";
						}
						bodyLabel.setText( home );
					}
				}
				);
	}
}