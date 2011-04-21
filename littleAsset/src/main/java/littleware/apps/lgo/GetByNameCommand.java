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
import com.google.inject.Inject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;
import littleware.security.LittleUser;

public class GetByNameCommand extends AbstractAssetCommand<GetByNameCommand.Input, Asset> {

    private static final Logger log = Logger.getLogger(GetByNameCommand.class.getName());

    public static class Input {

        private final String name;
        private final AssetType assetType;

        public Input(String name, AssetType assetType) {
            this.name = name;
            this.assetType = assetType;
        }

        public AssetType getAssetType() {
            return assetType;
        }

        public String getName() {
            return name;
        }
    }

    public static class Builder extends AbstractLgoBuilder<Input> {

        private final AssetSearchManager search;
        private final HumanPicklerProvider pickleProvider;

        private enum Option {
            name, type;
        };

        @Inject
        public Builder(HumanPicklerProvider pickleProvider,
                AssetSearchManager search) {
            super(GetByNameCommand.class.getName());
            this.pickleProvider = pickleProvider;
            this.search = search;
        }

        @Override
        public GetByNameCommand buildSafe(Input input) {
            return new GetByNameCommand(pickleProvider, search, input);
        }

        @Override
        public GetByNameCommand buildFromArgs(List<String> args) {
            final Map<String, String> mapDefault =
                    ImmutableMap.of(Option.name.toString(), "",
                    Option.type.toString(), LittleUser.USER_TYPE.toString());
            final Map<String, String> mapArg = processArgs(args, mapDefault);
            final String sName = mapArg.get(Option.name.toString());
            final String sType = mapArg.get(Option.type.toString()).toLowerCase();
            AssetType type = AssetType.UNKNOWN;
            for (AssetType possible : AssetType.getMembers()) {
                String sPossible = possible.toString().toLowerCase();
                log.log(Level.FINE, "Scanning type argument {0} ?= {1}", new Object[]{sType, sPossible});
                if (sType.equals(sPossible)
                        || (sPossible.endsWith(sType)
                        && possible.isNameUnique())) {
                    type = possible;
                    break;
                }
            }
            if (!type.isNameUnique()) {
                throw new IllegalArgumentException("Type is not name unique: " + type);
            }
            return buildSafe(new Input(sName, type));
        }
    }
    private final AssetSearchManager search;

    @Inject
    public GetByNameCommand(
            HumanPicklerProvider providePickler,
            AssetSearchManager search,
            Input input) {
        super(GetByNameCommand.class.getName(), providePickler, input);
        this.search = search;
    }

    @Override
    public Asset runCommand(Feedback feedback) throws Exception {
        final Input input = getInput();
        return search.getByName(input.getName(), input.getAssetType()).get();
    }
}
