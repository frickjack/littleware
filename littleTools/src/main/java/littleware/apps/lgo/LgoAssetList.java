/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import com.google.common.collect.ImmutableList;
import com.google.inject.ImplementedBy;
import java.util.Iterator;
import java.util.List;
import littleware.asset.Asset;

/**
 * Gson-friendly asset-list wrapper -
 * fascilitates JSON serialization of lgo-command results.
 * Lgo-commands should return LgoAssetList rather than Collection of assets
 * when possible to benefit from automatic JSON serialization in
 * the lgo server.
 */
public interface LgoAssetList extends Iterable<Asset> {
    public List<Asset> getList();
    /**
     * toString leverages HumanPicklerProvider under the hood
     */
    @Override
    public String toString();

    @ImplementedBy(SimpleAssetListBuilder.class)
    public interface AssetListBuilder {
        public AssetListBuilder add( Asset value );
        public AssetListBuilder addAll( Iterable<? extends Asset> value );
        public LgoAssetList build();
    }
}
