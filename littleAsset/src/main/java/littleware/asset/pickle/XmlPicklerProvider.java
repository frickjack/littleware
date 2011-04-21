/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.pickle;

/**
 * Factory for PickleHuman that supports AssetType based
 * specialization of serialization - tries to implement a flywheel pattern.
 * OSGi activators should register custom picklers at startup time.
 * Concurrent calls to get() are thread safe, but registerSpecializer
 * is not safe.
 */
public interface XmlPicklerProvider extends PicklerRegistry<AssetXmlPickler> {}
