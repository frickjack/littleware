package littleware.asset.internal;

import java.util.UUID;
import littleware.asset.AssetPathByRootId;
import littleware.asset.AssetPathFactory;



/**
 * Simple implementation of AssetPathByRootId interface.
 */
public class SimpleAssetPathByRootId extends AbstractAssetPath implements AssetPathByRootId {
    private static final long serialVersionUID = -1220806190088603807L;
    private final UUID   rootId;
    
    
    public SimpleAssetPathByRootId ( UUID rootId, String subrootPath, AssetPathFactory pathFactory ) {
        super ( "/byid:" + rootId.toString () + "/" + subrootPath, pathFactory );
        this.rootId = rootId;
    }
    
    
    @Override
    public UUID getRootId () {
        return rootId;
    }
            
}
