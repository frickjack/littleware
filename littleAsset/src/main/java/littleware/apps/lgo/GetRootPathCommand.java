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
import littleware.lgo.LgoCommand;
import com.google.inject.Inject;
import java.util.Map;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;

/**
 * Command tries to resolve home-rooted path for given asset-path.
 * See AssetPathFactory.toRootedPath
 */
public class GetRootPathCommand extends AbstractLgoCommand<AssetPath, AssetPath> {

    private final AssetPathFactory pathFactory;

    @Inject
    public GetRootPathCommand(
            AssetPathFactory pathFactory,
            AssetPath input) {
        super(GetRootPathCommand.class.getName(), input);
        this.pathFactory = pathFactory;
    }

    public static class Builder extends AbstractLgoBuilder<AssetPath> {

        private enum Option {
            path;
        }

        private final AssetPathFactory pathFactory;

        @Inject
        public Builder(AssetPathFactory pathFactory) {
            super(GetRootPathCommand.class.getName());
            this.pathFactory = pathFactory;
        }

        @Override
        public GetRootPathCommand buildSafe(AssetPath input) {
            return new GetRootPathCommand(pathFactory, input);
        }

        @Override
        public GetRootPathCommand buildFromArgs(List<String> args) {
            final Map<String, String> mapOptions = ImmutableMap.of(Option.path.toString(), "");
            final Map<String, String> mapArgs = processArgs(args, mapOptions);
            final String sPath = mapArgs.get(Option.path.toString());

            if (Whatever.get().empty(sPath)) {
                throw new IllegalArgumentException("Must specify path to asset to delete");
            }
            try {
                return buildSafe(pathFactory.createPath(sPath));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to parse path: " + sPath, ex);
            }
        }
    }

    @Override
    public AssetPath runCommand(Feedback feedback) throws Exception {
        return pathFactory.toRootedPath(getInput());
    }
}
