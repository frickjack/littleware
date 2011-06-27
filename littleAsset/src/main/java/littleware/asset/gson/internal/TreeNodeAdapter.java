/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.gson.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.TreeNode;
import littleware.asset.gson.AbstractAssetAdapter;


public class TreeNodeAdapter extends AbstractAssetAdapter {
    @Inject
    public TreeNodeAdapter( Provider<TreeNode.TreeNodeBuilder> builderFactory ) {
        super( TreeNode.TREE_NODE_TYPE, builderFactory );
    }
}