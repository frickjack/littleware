/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import java.util.List;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;

/**
 * Create a generic asset that inherits most properties from its parent
 */
public class CreateFolderCommand extends AbstractAssetCommand<CreateFolderCommand.Input, Asset> {

    public static class Services {

        private final AssetSearchManager search;
        private final AssetPathFactory pathFactory;
        private final AssetManager assetMgr;
        private final HumanPicklerProvider pickleProvider;
        private final Provider<TreeNode.TreeNodeBuilder> nodeProvider;

        public HumanPicklerProvider getPickleProvider() {
            return pickleProvider;
        }

        public Provider<TreeNode.TreeNodeBuilder> getNodeProvider() {
            return nodeProvider;
        }

        public AssetManager getAssetMgr() {
            return assetMgr;
        }

        public AssetPathFactory getPathFactory() {
            return pathFactory;
        }

        public AssetSearchManager getSearch() {
            return search;
        }

        @Inject
        public Services(AssetSearchManager search,
                AssetManager assetMgr,
                AssetPathFactory pathFactory,
                HumanPicklerProvider pickleProvider,
                Provider<TreeNode.TreeNodeBuilder> nodeProvider) {
            this.search = search;
            this.assetMgr = assetMgr;
            this.pathFactory = pathFactory;
            this.pickleProvider = pickleProvider;
            this.nodeProvider = nodeProvider;
        }
    }

    public static class Input {

        private final AssetType assetType;
        private final AssetPath path;
        private final String comment;

        public Input(AssetPath path, String comment, AssetType assetType) {
            this.path = path;
            this.comment = comment;
            this.assetType = assetType;
        }

        public AssetType getAssetType() {
            return assetType;
        }

        public String getComment() {
            return comment;
        }

        public AssetPath getPath() {
            return path;
        }
    }
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final AssetPathFactory pathFactory;
    private final Provider<TreeNode.TreeNodeBuilder> nodeProvider;

    /**
     * Allow subtypes to specialize based on asset-type
     */
    public CreateFolderCommand(String lgoName,
            Services services,
            Input input) {
        super(lgoName, services.getPickleProvider(), input);
        this.search = services.getSearch();
        this.assetMgr = services.getAssetMgr();
        this.pathFactory = services.getPathFactory();
        this.nodeProvider = services.getNodeProvider();
    }

    public static class Builder extends AbstractLgoBuilder<Input> {

        private final AssetType defaultType;

        public enum Option {

            path, comment, atype;
        }
        private final Services services;

        protected Builder(String lgoName, Services services, AssetType defaultType) {
            super(lgoName);
            this.services = services;
            this.defaultType = defaultType;
        }

        @Inject
        public Builder(Services services) {
            this(CreateFolderCommand.class.getName(), services, TreeNode.TREE_NODE_TYPE);
        }

        @Override
        public CreateFolderCommand buildSafe(Input input) {
            return new CreateFolderCommand(getName(), services, input);
        }

        @Override
        public CreateFolderCommand buildFromArgs(List<String> args) {
            final Map<String, String> mapDefault = new HashMap<String, String>();
            mapDefault.put(Option.path.toString(), "");
            mapDefault.put(Option.comment.toString(), "no comment");
            mapDefault.put(Option.atype.toString(), defaultType.getName());

            final Map<String, String> mapArgs = processArgs(args, mapDefault);
            for (Option opt : Option.values()) {
                if (Whatever.get().empty(mapArgs.get(opt.toString()))) {
                    throw new IllegalArgumentException("Missing required argument: " + opt);
                }
            }
            final AssetPath path;
            try {
                path = services.getPathFactory().createPath(mapArgs.get(Option.path.toString()));
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to parse path: " + mapArgs.get(Option.path.toString()));
            }
            final String typeName = mapArgs.get(Option.atype.toString()).toLowerCase();
            AssetType assetType = null;
            for (AssetType scan : AssetType.getMembers()) {
                if (scan.getName().toLowerCase().indexOf(typeName) >= 0) {
                    assetType = scan;
                    break;
                }
            }
            if (null == assetType) {
                throw new IllegalArgumentException("Unable to map asset type: " + typeName);
            }
            return buildSafe(new Input(path, mapArgs.get(Option.comment.toString()), assetType));
        }
    }

    /**
     * Create the folder at /parent/name,
     * where sNameIn is the default name if -name argument not given.
     *
     * @param feedback
     * @param sDefaultPath
     * @return id of new asset
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public Asset runCommand(Feedback feedback) throws Exception {
        final Input input = getInput();
        final Asset parent = search.getAssetAtPath(
                input.getPath().getParent()).get().narrow();
        final Asset asset;
        if (parent instanceof TreeNode) {
            asset = this.nodeProvider.get().parent((TreeNode) parent).name(input.getPath().getBasename()).comment(input.getComment()).build();
        } else {
            asset = this.nodeProvider.get().parent((LittleHome) parent).name(input.getPath().getBasename()).comment(input.getComment()).build();
        }
        return assetMgr.saveAsset(asset, input.getComment());
    }
}
