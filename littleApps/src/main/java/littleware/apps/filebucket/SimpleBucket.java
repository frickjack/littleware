package littleware.apps.filebucket;

import java.util.UUID;
import java.util.SortedSet;
import java.util.Collections;


/**
 * Simple implementation of Bucket interface
 */
public class SimpleBucket implements Bucket {
    private UUID                ou_asset = null;
    private SortedSet<String>   ov_paths = null;
    
    /** Do-nothing constructor for serialization support */
    protected SimpleBucket () {}
    
    /** 
     * Normal internal constructor.  
     * Clients should access via BucketManager.
     *
     * @param u_asset this bucket is associated with
     * @param v_paths files in this  bucket
     */
    public SimpleBucket ( UUID u_asset, SortedSet v_paths ) {
        ou_asset = u_asset;
        ov_paths = Collections.unmodifiableSortedSet ( v_paths );
    }
    
    
    public UUID getAssetId () {
        return ou_asset;
    }
    
    public SortedSet<String> getPaths () {
        return ov_paths;
    }
}
