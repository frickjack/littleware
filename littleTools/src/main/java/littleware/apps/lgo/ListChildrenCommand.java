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

import littleware.lgo.AbstractLgoCommand;
import littleware.lgo.LgoCommand;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.Maybe;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;

/**
 * List the child assets under a given asset path by id,name
 */
public class ListChildrenCommand extends AbstractLgoCommand<ListChildrenCommand.Input, Map<String, UUID>> {

    public static class Input {

        private final AssetPath path;
        private final Option<AssetType> maybeType;

        public Input(AssetPath path, AssetType childType) {
            this.path = path;
            this.maybeType = Maybe.emptyIfNull(childType);
        }

        public Input(AssetPath path) {
            this(path, null);
        }

        public AssetPath getPath() {
            return path;
        }

        public Option<AssetType> getChildType() {
            return maybeType;
        }
    }

    public static class Builder extends AbstractLgoBuilder<ListChildrenCommand.Input> {

        private final AssetPathFactory pathFactory;
        private final AssetSearchManager search;

        @Inject
        public Builder(AssetPathFactory pathFactory,
                AssetSearchManager search) {
            super(ListChildrenCommand.class.getName());
            this.pathFactory = pathFactory;
            this.search = search;
        }

        @Override
        public ListChildrenCommand buildSafe(Input input) {
            return new ListChildrenCommand(pathFactory, search, input);
        }

        @Override
        public ListChildrenCommand buildFromArgs(List<String> args) {
            final String sPathOption = "path";
            final String sTypeOption = "type";
            final Map<String, String> mapOpt = new HashMap<String, String>();
            mapOpt.put(sPathOption, null);
            mapOpt.put(sTypeOption, null);
            final Map<String, String> mapArgs = processArgs( args, mapOpt );
            final String sPath = mapArgs.get(sPathOption);
            final Option<String> maybeTypeName = Maybe.emptyIfNull(mapArgs.get(sTypeOption));
            if (Whatever.get().empty(sPath)) {
                throw new IllegalArgumentException("Must specify path to list children under");
            }
            Option<AssetType> maybeType = Maybe.empty();
            if (maybeTypeName.isSet() && (!Whatever.get().empty(maybeTypeName.get()))) {
                // lookup asset-type
                final String typeName = maybeTypeName.get().toLowerCase().trim();
                for (AssetType scan : AssetType.getMembers()) {
                    if (scan.toString().toLowerCase().equals(typeName)) {
                        maybeType = Maybe.something(scan);
                        break;
                    }
                }
            }
            try {
                return buildSafe(new Input(pathFactory.createPath(sPath), maybeType.getOr(null)));
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException( "Failed to process options", ex );
            }
        }
    }
    private final AssetSearchManager search;
    private final AssetPathFactory pathFactory;

    @Inject
    public ListChildrenCommand(
            AssetPathFactory pathFactory,
            AssetSearchManager search,
            Input input) {
        super(ListChildrenCommand.class.getName(), input);
        this.search = search;
        this.pathFactory = pathFactory;
    }

    @Override
    public Map<String, UUID> runCommand(Feedback feedback) throws Exception {
        final Input data = getInput();

        return search.getAssetIdsFrom(
                search.getAssetAtPath(data.getPath()).get().getId(),
                data.getChildType().getOr(null));
    }

    @Override
    public String runCommandLine(Feedback feedback) throws Exception {
        final Input argData = getInput();
        final Map<String, UUID> mapChildren = runCommand(feedback);
        final List<String> vChildren = new ArrayList<String>(mapChildren.keySet());
        Collections.sort(vChildren);
        final StringBuilder sb = new StringBuilder();
        for (String sChild : vChildren) {
            sb.append(mapChildren.get(sChild).toString()).append(",").
                    append(argData.getPath().toString()).append("/").append(sChild).
                    append(Whatever.NEWLINE);
        }
        return sb.toString();
    }
}
