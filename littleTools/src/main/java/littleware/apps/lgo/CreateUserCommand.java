/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import java.util.List;
import littleware.lgo.LgoException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Map;
import java.util.logging.Level;
import littleware.asset.client.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.lgo.AbstractLgoBuilder;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.LittleUser;

/**
 * Create a new littleware.USER asset at /littleware.home/Users/name.
 * Return the new user's objectid.
 */
public class CreateUserCommand extends AbstractAssetCommand<CreateUserCommand.Input, LittleUser> {

    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final AssetPathFactory pathFactory;
    private final Provider<LittleUser.Builder> userProvider;

    public static class Input {

        private final boolean admin;
        private final String name;

        public boolean isAdmin() {
            return admin;
        }

        public String getName() {
            return name;
        }

        public Input(String name, boolean admin) {
            this.name = name;
            this.admin = admin;
        }
    }

    public static class Builder extends AbstractLgoBuilder<Input> {

        private final HumanPicklerProvider pickleProvider;
        private final AssetSearchManager search;
        private final AssetManager assetMgr;
        private final AssetPathFactory pathFactory;
        private final Provider<LittleUser.Builder> userProvider;

        private enum Option {

            name, admin
        };

        @Inject
        public Builder(AssetSearchManager search, AssetManager assetMgr,
                AssetPathFactory pathFactory, HumanPicklerProvider pickleProvider,
                Provider<LittleUser.Builder> userProvider ) {
            super(CreateUserCommand.class.getName());
            this.search = search;
            this.assetMgr = assetMgr;
            this.pathFactory = pathFactory;
            this.pickleProvider = pickleProvider;
            this.userProvider = userProvider;
        }

        @Override
        public CreateUserCommand buildSafe(Input input) {
            return new CreateUserCommand(this, input, userProvider );
        }

        @Override
        public CreateUserCommand buildFromArgs(List<String> args) {
            final Map<String, String> mapArg = processArgs(args,
                    Option.name.toString(), Option.admin.toString());

            final String name = mapArg.get(Option.name.toString());
            if (Whatever.get().empty(name)) {
                throw new IllegalArgumentException("Required argument --name not set");
            }
            final boolean admin = !Whatever.get().empty(mapArg.get(Option.admin.toString()));
            return buildSafe( new Input( name, admin ) );
        }
    }

    public CreateUserCommand(
            Builder builder,
            Input input,
            Provider<LittleUser.Builder> userProvider ) {
        super(CreateUserCommand.class.getName(), builder.pickleProvider, input);
        this.search = builder.search;
        this.assetMgr = builder.assetMgr;
        this.pathFactory = builder.pathFactory;
        this.userProvider = userProvider;
    }

    @Override
    public LittleUser runCommand(Feedback feedback) throws Exception {
        final String sFolder = "/littleware.home/Users";
        final Input input = getInput();
        final TreeParent folder;
        try {
            folder = search.getAssetAtPath(
                    pathFactory.createPath(sFolder)).get().narrow();
        } catch (Exception ex) {
            throw new LgoException("Failed to load parent folder: " + sFolder, ex);
        }
        final LittleUser userNew = assetMgr.saveAsset(
                    userProvider.get().parent((TreeNode) folder ).name(input.getName()).build(), "CreateUserCommand").narrow();
        if ( input.isAdmin() ) {
            try {
                final LittleGroup groupAdmin = search.getAsset(
                        AccountManager.UUID_ADMIN_GROUP
                        ).get().narrow();
                assetMgr.saveAsset(groupAdmin.copy().narrow( LittleGroup.Builder.class ).add(userNew).build(), "Added user " + userNew.getName() );
            } catch (Exception ex) {
                feedback.log(Level.SEVERE,
                        "Failed to add new user " + userNew.getName() + " to admin group");
            }
        }
        return userNew;
    }
}
