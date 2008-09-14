package littleware.asset;

/**
 * Exception for CacheManager implementations to throw
 * on request to lookup a query not yet in the cache.
 */
public class CacheMissException extends littleware.asset.AssetException {
	public CacheMissException () {}
	
	public CacheMissException ( String s_message ) {
		super ( s_message );
	}
	
	/** Constructor with exception chaining */
	public CacheMissException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

