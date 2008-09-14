package littleware.apps.tracker.server;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.tracker.*;
import littleware.asset.*;
import littleware.asset.server.AssetResourceBundle;
import littleware.base.BaseException;


/**
* Resource bundle under littleware.apps.tracker package
 */
public class TrackerResourceBundle extends ListResourceBundle {
	private static final Logger         olog_generic = Logger.getLogger ( "littleware.apps.tracker.server.TrackerResourceBundle" );
	
	
	private static Object[][] ov_contents = {
	{ "littleware_tracker_manager", null },
	};
	
	static {
		try {
			// Need to make sure the littleware.asset ResourceBundle is initialized first
            AssetResourceBundle bundle_asset = AssetResourceBundle.getBundle ();
			AssetSearchManager  m_search = (AssetSearchManager) bundle_asset.getObject ( AssetResourceBundle.Content.AssetSearcher );
            AssetManager        m_asset = (AssetManager) bundle_asset.getObject ( AssetResourceBundle.Content.AssetManager );
			
			ov_contents[0][1] = new SimpleTrackerManager ( m_asset, m_search );
		} catch ( RuntimeException e ) {
			olog_generic.log ( Level.WARNING, "Failed initialization, caught: " + e + ", " +
							   BaseException.getStackTrace ( e )
							   );
			throw e;
		}
	}
	
	/** Do nothing constructor */
	public TrackerResourceBundle () {
		super ();
	}
	
	/**
     * Implements ListResourceBundle's one abstract method -
	 * ListResourceBundle takes care of the rest of the ResourceBundle interface.
	 */
	public Object[][] getContents() {
		return ov_contents;
	}	
	
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

