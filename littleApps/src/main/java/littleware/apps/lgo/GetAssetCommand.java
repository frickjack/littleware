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
import littleware.lgo.LgoCommand;
import com.google.inject.Inject;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;

/**
 * Get the asset at a given path
 */
public class GetAssetCommand extends AbstractAssetCommand<AssetPath, Asset> {

    public static class Builder extends AbstractLgoBuilder<AssetPath> {

        private final AssetSearchManager search;
        private final AssetPathFactory pathFactory;
        private final HumanPicklerProvider pickleProvider;

        @Inject
        public Builder(AssetSearchManager search,
                AssetPathFactory pathFactory,
                HumanPicklerProvider pickleProvider) {
            super(GetAssetCommand.class.getName());
            this.search = search;
            this.pathFactory = pathFactory;
            this.pickleProvider = pickleProvider;
        }

        @Override
        public GetAssetCommand buildSafe(AssetPath path) {
            return new GetAssetCommand(pickleProvider, search, path);
        }

        @Override
        public GetAssetCommand buildFromArgs(List<String> args) {
            final String sPath = processArgs(args, "path").get("path");
            try {
                return buildSafe(pathFactory.createPath(sPath));
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to load path: " + sPath, ex);
            }
        }
    }
    private final AssetSearchManager search;

    @Inject
    public GetAssetCommand(
            HumanPicklerProvider providePickler,
            AssetSearchManager search,
            AssetPath path) {
        super(GetAssetCommand.class.getName(), providePickler, path);
        this.search = search;
    }

    /**
     * Lookup the asset at the given path
     *
     * @param feedback
     * @param in path to lookup if --path argument not given
     * @return
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public Asset runCommand(Feedback feedback) throws Exception {
        final AssetPath in = getInput();
        return search.getAssetAtPath(in).get();
    }
}
