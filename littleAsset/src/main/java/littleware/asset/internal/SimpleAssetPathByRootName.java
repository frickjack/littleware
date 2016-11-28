package littleware.asset.internal;

import littleware.asset.AssetPathByRootName;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.asset.LittleHome;



/**
 * Simple implementatin of AssetPathByRootName interface.
 */
public class SimpleAssetPathByRootName extends AbstractAssetPath implements AssetPathByRootName {
    private static final long serialVersionUID = -1141969192993296586L;
    private AssetType  rootType = null;
    private String     rootName = null;
    
    
    /**
     * Setup the path.
     *
     * @throws InvalidAssetTypeException unless n_type.isNameUnique()
     */
    public SimpleAssetPathByRootName ( AssetType n_type, 
                                       String s_root_name,
                                       String s_subroot_path,
                                       AssetPathFactory pathFactory
                                       ) throws InvalidAssetTypeException
    {
        super ( ( n_type.equals(LittleHome.HOME_TYPE) ?
                    ("/" + s_root_name) :
                    ("/byname:" + s_root_name + ":type:" + n_type)
                ) + "/" + s_subroot_path,
                pathFactory
                );

        rootType = n_type;
        rootName = s_root_name;
        if ( ! n_type.isNameUnique () ) {
            throw new InvalidAssetTypeException ( "Asset type not name unique: " + n_type );
        }
    }
    
    /**
     * Get the AssetType of the root asset
     */
    @Override
    public AssetType getRootType () {
        return rootType;
    }
    
    @Override
    public String getRootName () {
        return rootName;
    }
    
    
}

