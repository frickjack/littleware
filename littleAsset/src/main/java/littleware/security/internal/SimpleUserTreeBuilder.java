/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.TreeNode;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.base.validate.ValidationException;
import littleware.security.LittleUser;
import littleware.security.LittleUser.Builder;
import littleware.security.UserTreeBuilder;

/**
 * Simple UserTreeBuilder implementation sets up tree-template
 * with format parent/Users/1st-letter/user
 */
public class SimpleUserTreeBuilder implements UserTreeBuilder {

    private String userName = null;
    private final Provider<TemplateBuilder> treeBuilder;
    private final Provider<TreeNodeBuilder> nodeProvider;
    private final Provider<Builder> userProvider;

    @Inject()
    public SimpleUserTreeBuilder(Provider<AssetTreeTemplate.TemplateBuilder> treeBuilder,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            Provider<LittleUser.Builder> userProvider
            ) {
        this.treeBuilder = treeBuilder;
        this.nodeProvider = nodeProvider;
        this.userProvider = userProvider;
    }

    @Override
    public UserTreeBuilder user(String value) {
        if (!value.matches("^\\w+$")) {
            throw new IllegalArgumentException("Illegal username: " + value);
        }
        userName = value;
        return this;
    }

    @Override
    public AssetTreeTemplate build() {
        ValidationException.validate(null != userName, "null user");
        return treeBuilder.get().assetBuilder("Users").addChildren(
                treeBuilder.get().assetBuilder(userName.toUpperCase().substring(0, 1)).addChildren(
                treeBuilder.get().assetBuilder( userProvider.get().name(userName)).build()).build()).build();
    }
}
