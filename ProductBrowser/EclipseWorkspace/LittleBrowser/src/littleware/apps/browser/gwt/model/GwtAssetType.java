/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.browser.gwt.model;

/**
 * GWT adaptation of AssetType
 */
public class GwtAssetType implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	private GwtUUID     id;
	private String      name;
	
	private GwtAssetType() {}
	
	private GwtAssetType( GwtUUID id, String name ) {
		this.id = id;
		this.name = name;
	}
	
	public GwtUUID getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	public static GwtAssetType build( String name, GwtUUID id ) {
		return new GwtAssetType( id, name );
	}
	
}
