/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

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
