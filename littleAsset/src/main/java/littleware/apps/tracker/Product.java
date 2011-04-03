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

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;

/**
 * Product node
 */
public interface Product extends Asset {

    /**
     * Shortcut for search.getIdsFrom( QueueType )
     */
    public UUID getTaskQueue() throws BaseException, GeneralSecurityException, RemoteException;

    /** 
     * Collection of dependencies on other (non-sub) products.
     * Shortcut for search.getIdsFrom( ProductAlias )
     */
    public Map<String, UUID> getDepends() throws BaseException, GeneralSecurityException, RemoteException;

    /**
     * Shortcut for search.getIdsFrom( ProductType )
     */
    public Map<String, UUID> getSubProducts() throws BaseException, GeneralSecurityException, RemoteException;

    /**
     * Shortcut for search.getIdsFrom( Versions )
     */
    public Map<String, UUID> getVersions() throws BaseException, GeneralSecurityException, RemoteException;

    public Map<String, UUID> getVersionAliases() throws BaseException, GeneralSecurityException, RemoteException;

    @Override
    public ProductBuilder copy();

    public interface ProductBuilder extends AssetBuilder {
        @Override
        public ProductBuilder name( String value );
        @Override
        public ProductBuilder copy(Asset value);

        @Override
        public ProductBuilder parent(Asset value);

        @Override
        public Product build();
    }

    public static class Type extends AssetType {

        private Type() {
            super(
                    UUIDFactory.parseUUID("337291A8485742E987BCB225A33FDF2F"),
                    "littleware.Product");
        }

        @Override
        public ProductBuilder create() {
            return new SimpleProductBuilder();
        }
    }
    public static final Type ProductType = new Type();
}
