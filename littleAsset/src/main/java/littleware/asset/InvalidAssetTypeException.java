package littleware.asset;

/**
 * Exception thrown on attempt to perform some
 * operation with an asset of inappropriate type.
 */
public class InvalidAssetTypeException extends AssetException {
    
	private static final long serialVersionUID = 1L;

	/** Goofy default constructor */
	public InvalidAssetTypeException () {
		super ( "Asset type not appropriate for operation" );
	}
	
	/** Constructor with user-supplied message */
	public InvalidAssetTypeException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public InvalidAssetTypeException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

