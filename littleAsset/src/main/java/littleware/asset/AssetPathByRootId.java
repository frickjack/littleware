package littleware.asset;

import java.util.UUID;


/**
 * Specialization of AssetPath that identifies its root
 * by the asset id.  For example: <br />
 *      /byid:2D2B669E-F4F0-4A06-9D77-5E6772752DF8/bla/bla
 */
public interface AssetPathByRootId extends AssetPath {
    /**
     * Get the name of the asset at the root of this path
     */
    public UUID getRootId ();
    
}
