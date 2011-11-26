package littleware.apps.browser.gwt.model;


/**
 * Marker interface for GwtUser, GwtGroup, and other identity types
 */
public interface GwtPrincipal {
	public String getName();
	public GwtUUID  getId();
}
