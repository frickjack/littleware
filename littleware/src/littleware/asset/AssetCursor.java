package littleware.asset;

import java.util.Iterator;

/** 
 * Slight specialization of Iterator to allow us to 
 * add some new methods in the future to batch-return methods.
 */
public interface AssetCursor<T> extends Iterator<T> {

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

