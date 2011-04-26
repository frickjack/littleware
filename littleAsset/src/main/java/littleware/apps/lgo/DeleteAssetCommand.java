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

import com.google.common.collect.ImmutableMap;
import java.util.List;
import littleware.lgo.AbstractLgoCommand;
import com.google.inject.Inject;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.client.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;

/**
 * Simple delete-asset command - refuses to delete asset
 * that has child assets linking from it.
 * Takes single string argument that specifies the AssetPath to
 * delete:
 *      lgo delete -path [path] -comment [delete_comment]
 */
public class DeleteAssetCommand extends AbstractLgoCommand<DeleteAssetCommand.Input, UUID> {

    public static class Input {

        private final AssetPath path;
        private final String comment;

        public Input(AssetPath path, String comment) {
            this.path = path;
            this.comment = comment;
        }

        public String getComment() {
            return comment;
        }

        public AssetPath getPath() {
            return path;
        }
    }

    public static class Builder extends AbstractLgoBuilder<Input> {

        private final AssetPathFactory pathFactory;
        private final AssetManager assetMgr;
        private final AssetSearchManager search;

        @Inject
        public Builder(AssetSearchManager search, AssetManager assetMgr,
                AssetPathFactory pathFactory) {
            super(DeleteAssetCommand.class.getName());
            this.search = search;
            this.assetMgr = assetMgr;
            this.pathFactory = pathFactory;
        }

        private enum Option {

            path, comment;
        }

        @Override
        public DeleteAssetCommand buildSafe(Input input) {
            return new DeleteAssetCommand(this, input);
        }

        @Override
        public DeleteAssetCommand buildFromArgs(List<String> args) {
            final Map<String, String> mapOptions = ImmutableMap.of(
                    Option.path.toString(), "",
                    Option.comment.toString(), "No comment"
                    );

            final Map<String, String> mapArgs = processArgs(args, mapOptions);
            final String sPath = mapArgs.get(Option.path.toString());
            final String sComment = mapArgs.get(Option.comment.toString());

            if (Whatever.get().empty(sPath)) {
                throw new IllegalArgumentException("Must specify path to asset to delete");
            }
            final AssetPath path;
            try {
                path = pathFactory.createPath(sPath);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to parse path: " + sPath, ex);
            }
            return buildSafe(new Input(path, sComment));
        }
    }
    private AssetSearchManager search;
    private AssetPathFactory pathFactory;
    private AssetManager assetMgr;

    @Inject
    public DeleteAssetCommand(Builder builder,
            Input input) {
        super(DeleteAssetCommand.class.getName(), input);
        this.search = builder.search;
        this.pathFactory = builder.pathFactory;
        this.assetMgr = builder.assetMgr;
    }

    @Override
    public UUID runCommand(Feedback feedback) throws Exception {
        final Input input = getInput();
        final Asset aDelete;

        aDelete = search.getAssetAtPath(input.getPath()).get();

        if (!search.getAssetIdsFrom(aDelete.getId(), null).isEmpty()) {
            throw new IllegalArgumentException("May not delete asset with child assets linking from it: " + input.getPath());
        }

        assetMgr.deleteAsset(aDelete.getId(), input.getComment());
        return aDelete.getId();
    }
}
