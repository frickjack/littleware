/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.browser.gwt.model;

/**
 * A GWT version of java.util.UUID
 *
 */
public class GwtUUID implements java.io.Serializable {
	private String id = "gwt" + rand.nextLong() + (new java.util.Date()).getTime();

	private GwtUUID() {}
	private GwtUUID( String id ) {
		this.id = id;
	}
	
	@Override
	public boolean equals( Object other ) {
		return (null != other) && (other instanceof GwtUUID) &&
				((GwtUUID) other).id.equals( id );
	}
	
	@Override
	public String toString() { return id; }
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	//----------------------------------------
	private static final java.util.Random rand = new java.util.Random();
	private static final long serialVersionUID = 1L;

	public static GwtUUID randomId() {
		return new GwtUUID();
	}
	
	public static GwtUUID fromString( String id ) {
		return new GwtUUID( id );
	}

}
