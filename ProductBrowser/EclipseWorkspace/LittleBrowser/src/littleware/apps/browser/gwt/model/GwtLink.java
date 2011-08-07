package littleware.apps.browser.gwt.model;

public interface GwtLink extends GwtAsset {

    /** LINK assset-type */
    public static final GwtAssetType LINK_TYPE = GwtAssetType.build(
    		"littleware.LINK",
    		GwtUUID.fromString("926D122F82FE4F28A8F5C790E6733665")
            );
    
    public GwtUUID getFromId();
    public GwtUUID getToId();
	
}
