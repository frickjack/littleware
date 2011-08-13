/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.browser.gwt.client;

import littleware.apps.browser.gwt.client.controller.action.GreetingAction;
import littleware.apps.browser.gwt.client.view.BrowserPanelView;
import littleware.apps.browser.gwt.client.view.DemoPanelView;
import littleware.apps.browser.gwt.controller.GreetingService;
import littleware.apps.browser.gwt.controller.GreetingServiceAsync;
import littleware.apps.browser.gwt.controller.internal.AssetSearchService;
import littleware.apps.browser.gwt.controller.internal.AssetSearchServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class LittleBrowser implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);
	private final AssetSearchServiceAsync  search = GWT.create( AssetSearchService.class );

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
	    final DockLayoutPanel dock = new DockLayoutPanel(Unit.EM);
	    dock.addNorth(new HTML("<h2>LittleBrowser</h2>"), 4);
	    //dock.addSouth(new HTML("south"), 2);
	    //dock.addEast(new HTML("east"), 2);
	    dock.addWest(new HTML("Navigation"), 10 );
	
		final DemoPanelView    dview = new DemoPanelView( "Reuben" );
		final BrowserPanelView browser = new BrowserPanelView( search );
		
	    // Create a three-item tab panel, with the tab area 1.5em tall.
	    final TabLayoutPanel tabPanel = new TabLayoutPanel(1.5, Unit.EM);
	    //tabPanel.add(new HTML("this"), "[this]");
	    tabPanel.add( browser, "[browser]");
	    tabPanel.add(new HTML("that"), "[that]");
	    tabPanel.add( dview, "[demo]" );

	    dock.add( tabPanel );
	    // Attach the LayoutPanel to the RootLayoutPanel. The latter will listen for
	    // resize events on the window to ensure that its children are informed of
	    // possible size changes.
	    final RootLayoutPanel rp = RootLayoutPanel.get();
	    //final LayoutPanel rp = new LayoutPanel();
	    rp.add(dock);		
		//RootPanel.get( "littleUI" ).add( rp );

		// Focus the cursor on the name field when the app loads
		dview.nameField.setFocus(true);
		dview.nameField.selectAll();

		// Add a handler to send the name to the server
		final GreetingAction handler = new GreetingAction( greetingService, dview );
		dview.sendButton.addClickHandler(handler);
		dview.nameField.addKeyUpHandler(handler);		
	}
}
