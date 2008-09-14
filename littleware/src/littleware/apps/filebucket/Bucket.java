package littleware.apps.filebucket;

import java.util.UUID;
import java.util.SortedSet;


/**
 * Info on the filebucket assigned to an asset
 */
public interface Bucket extends java.io.Serializable {
    /**
     * Get the asset this bucket is associated with.
     */
    public UUID getAssetId ();
    
    /**
     * Get the set of file-paths stuffed in this bucket
     */
    public SortedSet<String> getPaths ();
}
