package littleware.asset;

/**
 * Little thunk scans an AssetTreeTemplate to check whether
 * the assets in the template already exist under the given parent.
 */
public interface TemplateScanner extends AssetTreeTemplate.TreeVisitor {

    /**
     * Little POJO bucket holds the asset is a node on the tree,
     * and the exists property states whether or not that
     * asset already exists in the repository.
     */
    public interface ExistInfo extends AssetTreeTemplate.AssetInfo {
        public boolean getAssetExists();
    }

    @Override
    public ExistInfo visit( TreeParent parent, AssetTreeTemplate template );
}
