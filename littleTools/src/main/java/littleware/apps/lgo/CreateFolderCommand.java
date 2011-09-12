/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import java.util.List;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.client.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;

/**
 * Create a generic asset that inherits most properties from its parent
 */
public class CreateFolderCommand extends AbstractAssetCommand<CreateFolderCommand.Input, Asset> {
    
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final AssetPathFactory pathFactory;
    private final Services services;

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
        this.services = services;
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
        final String assetName = input.getPath().getBasename();
        final AssetBuilder assetBuilder = this.services.getNodeBuilder( input.getAssetType() ).name( assetName ).comment(input.getComment());
        if( input.getAssetType().isA( LittleHome.HOME_TYPE )  ) {
            assetBuilder.setAclId( LittleAcl.UUID_EVERYBODY_READ );
        } else {
            final TreeParent parent = search.getAssetAtPath( input.getPath().getParent()).get().narrow();
            assetBuilder.narrow( TreeNode.TreeNodeBuilder.class ).parent( parent );
        }
        return assetMgr.saveAsset(assetBuilder.build(), input.getComment());
    }
    
    //--------------------------------------

    /**
     * Little bucket to simplify passing around properties
     * between the command-builder and the command instance
     */
    public static class Services {

        private final AssetSearchManager search;
        private final AssetPathFactory pathFactory;
        private final AssetManager assetMgr;
        private final HumanPicklerProvider pickleProvider;
        private final AssetProviderRegistry assetRegistry;

        public HumanPicklerProvider getPickleProvider() {
            return pickleProvider;
        }

        /**
         * Get a new default node provider
         */
        public TreeNode.TreeNodeBuilder getNodeBuilder() {
            return assetRegistry.getService( TreeNode.TREE_NODE_TYPE ).get().narrow();
        }

        /**
         * Get a new builder for the given asset type
         */
        public AssetBuilder getNodeBuilder( AssetType selector ) {
            return assetRegistry.getService( selector ).get().narrow();
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
                AssetProviderRegistry assetRegistry) {
            this.search = search;
            this.assetMgr = assetMgr;
            this.pathFactory = pathFactory;
            this.pickleProvider = pickleProvider;
            this.assetRegistry = assetRegistry;
        }
    }

    /**
     * User supplied input options
     */
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

    /**
     * Command builder
     */
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

        private final Set<AssetType>  legalTypes = new HashSet<AssetType>();
        {
            Collections.addAll( legalTypes, LittleHome.HOME_TYPE, TreeNode.TREE_NODE_TYPE, 
                    LittleGroup.GROUP_TYPE, LittleAcl.ACL_TYPE
                    );
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
            for (AssetType scan : legalTypes ) {
                if (scan.getName().toLowerCase().indexOf(typeName) >= 0) {
                    assetType = scan;
                    break;
                }
            }
            if (null == assetType) {
                throw new IllegalArgumentException("Unable to map asset name to legal type: " + typeName);
            }
            return buildSafe(new Input(path, mapArgs.get(Option.comment.toString()), assetType));
        }
    }
        

}
