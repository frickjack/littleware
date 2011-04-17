/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.AssetType;
import littleware.base.validate.ValidationException;

/**
 * Simple UserTreeBuilder implementation sets up tree-template
 * with format parent/Users/1st-letter/user
 */
public class SimpleUserTreeBuilder implements UserTreeBuilder {

    private String user = null;
    private final Provider<TemplateBuilder> treeBuilder;

    @Inject()
    public SimpleUserTreeBuilder(Provider<AssetTreeTemplate.TemplateBuilder> treeBuilder) {
        this.treeBuilder = treeBuilder;
    }

    @Override
    public UserTreeBuilder user(String value) {
        if (!value.matches("^\\w+$")) {
            throw new IllegalArgumentException("Illegal username: " + value);
        }
        user = value;
        return this;
    }

    @Override
    public AssetTreeTemplate build() {
        ValidationException.validate(null != user, "null user");
        return treeBuilder.get().assetBuilder(GenericAsset.GENERIC.create().name("Users")).addChildren(
                treeBuilder.get().assetBuilder(GenericAsset.GENERIC.create().name(user.toUpperCase().substring(0, 1))).addChildren(
                treeBuilder.get().assetBuilder(SecurityAssetType.USER.create().name(user)).build()).build()).build();
    }
}
