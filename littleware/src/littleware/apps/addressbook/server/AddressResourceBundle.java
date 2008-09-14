package littleware.apps.addressbook.server;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.addressbook.*;
import littleware.asset.AssetRetriever;
import littleware.asset.server.AssetResourceBundle;
import littleware.base.BaseException;


/**
 * Resource bundle under littleware.apps.addressbook package
 */
public class AddressResourceBundle extends ListResourceBundle {
	private static Logger         olog_generic = Logger.getLogger ( "littleware.apps.addressbook.server.AddressResourceBundle" );
	
	
	private static Object[][] ov_contents = {
	{ "littleware_address_specializer", null },
	};
	
	static {
		try {
			// Need to make sure the littleware.asset ResourceBundle is initialized first
			AssetResourceBundle bundle_asset = AssetResourceBundle.getBundle ();
			AssetRetriever m_retriever = (AssetRetriever) bundle_asset.getObject ( AssetResourceBundle.Content.AssetSearcher );
			
			ov_contents[0][1] = new AddressSpecializer ( m_retriever );
		} catch ( RuntimeException e ) {
			olog_generic.log ( Level.WARNING, "Failed initialization, caught: " + e + ", " +
							   BaseException.getStackTrace ( e )
							   );
			throw e;
		}
	}
	
	/** Do nothing constructor */
	public AddressResourceBundle () {
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

