/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.browser.gwt.model;

/**
 * GWT friendly adaptation of littleware.asset.Asset
 */
public abstract class GwtAsset {
	private String name = "unset";
	
	public GwtAsset() {}
	public String getName() { return name; }
	
	public static abstract class Builder {
		public abstract GwtAsset build();
	}
	
}
