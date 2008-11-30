package littleware.apps.tracker;

import littleware.asset.server.AssetSpecializer;
import java.util.*;

import littleware.asset.*;

import littleware.base.UUIDFactory;

/** 
 * AssetType specializer and bucket for littleware.apps.tracker
 * based AssetTypes.
 */
public abstract class TrackerAssetType<T extends Asset> extends AssetType<T> {
	private static AssetSpecializer   om_tracker = null;
	
	private static AssetSpecializer getSharedSpecializer () {
		if ( null == om_tracker ) {
			ResourceBundle bundle_address = ResourceBundle.getBundle ( "littleware.apps.tracker.server.TrackerResourceBundle" );
			om_tracker = (AssetSpecializer) bundle_address.getObject ( "littleware_tracker_manager" );
		}
		return om_tracker;
	}
	
	public static final AssetType<Comment> COMMENT    = new AssetType<Comment> (
																				UUIDFactory.parseUUID ( "FB8CC7B7C9324EC8953DE50A700344F3" ),
																				"littleware.apps.tracker.COMMENT"
                                                                                ) {

		public Comment create () { return new SimpleComment (); }
        @Override
		public boolean mustBeAdminToCreate () { return false; }
	};
    
    
    public static final AssetType<Dependency> DEPENDENCY    = new AssetType<Dependency> (
																				UUIDFactory.parseUUID ( "489F21E1D19B49F3B923E7B45609A811" ),
																				"littleware.apps.tracker.DEPENDENCY"
                                                                                ) {

		public Dependency create () { 
            return new SimpleDependency ();
        }
        @Override
		public boolean mustBeAdminToCreate () { return false; }
	};
    
	public static final AssetType<Task> TASK    = new AssetType<Task> (
																				UUIDFactory.parseUUID ( "84F04E04DCE947B2A00294949DC38628" ),
																				"littleware.apps.tracker.TASK"
																				) {

		public Task create () { return new SimpleTask (); }
        @Override
		public boolean mustBeAdminToCreate () { return false; }
	};	
    
    public static final AssetType<Queue> QUEUE    = new AssetType<Queue> (
                                                                       UUIDFactory.parseUUID ( "0FE9FBED5F6846E1865526A2BFBC5182" ),
                                                                       "littleware.apps.tracker.QUEUE"
                                                                       ) {

		public Queue create () { 
            return new SimpleQueue (); 
        }
        @Override
		public boolean mustBeAdminToCreate () { return false; }
	};	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

