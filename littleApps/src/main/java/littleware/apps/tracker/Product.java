/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;

/**
 * Product node
 */
public interface Product extends Asset {
    /**
     * Shortcut for search.getIdsFrom( QueueType )
     */
    public UUID  getTaskQueue();

    /** 
     * Collection of dependencies on other (non-sub) products.
     * Shortcut for search.getIdsFrom( ProductAlias )
     */
    public Map<String,UUID> getDepends();

    /**
     * Shortcut for search.getIdsFrom( ProductType )
     */
    public Map<String,UUID> getSubProducts();

    /**
     * Shortcut for search.getIdsFrom( Versions )
     */
    public Map<String,UUID> getVersions();
    public Map<String,UUID> getVersionAliases();

    @Override
    public ProductBuilder copy();

    public interface ProductBuilder extends AssetBuilder {
        @Override
        public ProductBuilder copy( Asset value );
        @Override
        public ProductBuilder parent( Asset value );
        @Override
        public Product build();
    }
}
