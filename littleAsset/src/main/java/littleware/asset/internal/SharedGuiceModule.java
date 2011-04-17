/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.internal;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.GenericAsset;
import littleware.asset.IdWithClock;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;

/**
 * Guice bindings common to both client and server bootstrap setups
 */
public class SharedGuiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind( LittleHome.HomeBuilder.class ).to( LittleHomeBuilder.class );
        binder.bind( GenericAsset.GenericBuilder.class ).to( SimpleGenericBuilder.class );
        binder.bind( TreeNode.TreeNodeBuilder.class ).to( SimpleTreeNodeBuilder.class );
        binder.bind( IdWithClock.Builder.class ).to( IdWithClockBuilder.class );
        binder.bind(AssetPathFactory.class).to(SimpleAssetPathFactory.class);
        binder.bind( AssetTreeTemplate.TemplateBuilder.class ).to( SimpleTemplateBuilder.class ).in( Scopes.SINGLETON );
    }

}
