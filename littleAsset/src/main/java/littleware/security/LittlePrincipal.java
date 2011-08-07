/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security;

import littleware.asset.AssetType;
import littleware.asset.TreeNode;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;

/**
 * Slight extention of Principal interface
 * to support notion of a principal id and comment
 */
public interface LittlePrincipal extends java.security.Principal, littleware.asset.TreeNode {

    /**
     * PRINCIPAL asset type - with AccountManager asset specializer
     * This asset-type is abstract - just intended for grouping
     * USER and GROUP types together.
     */
    public static final AssetType PRINCIPAL_TYPE = new AssetType(
            UUIDFactory.parseUUID("A7E11221-5469-49FA-AF1E-8FCC52190F1D"),
            "littleware.PRINCIPAL") {

        private final Option<AssetType> superType = Maybe.something( TreeNode.TREE_NODE_TYPE );

        @Override
        public Option<AssetType>  getSuperType() {
            return superType;
        }

    };

}

