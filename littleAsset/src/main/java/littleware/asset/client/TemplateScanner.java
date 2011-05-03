/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.base.BaseException;

/**
 * Little thunk scans an AssetTreeTemplate
 */
public interface TemplateScanner {

    /**
     * Little POJO bucket holds the asset is a node on the tree,
     * and the exists property states whether or not that
     * asset already exists in the repository.
     */
    public interface AssetInfo {
        public TreeNode getAsset();
        public boolean getAssetExists();
    }

    public Collection<AssetInfo> scan( TreeParent parent, AssetTreeTemplate treeTemplate ) throws BaseException, GeneralSecurityException, RemoteException;
}
