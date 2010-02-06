/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Arrays;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Lgo BundleActivator registers lgo commands with
 * the LgoCommandDictionary
 */
public class LgoActivator implements BundleActivator {


    /** Inject dependencies */
    @Inject
    public LgoActivator(
            LgoCommandDictionary commandMgr,
            LgoHelpLoader helpMgr,
            Provider<EzHelpCommand> comHelp,
            Provider<XmlEncodeCommand> comXml,
            Provider<LgoBrowserCommand> comBrowse,
            Provider<DeleteAssetCommand> comDelete,
            Provider<ListChildrenCommand> comLs,
            Provider<GetAssetCommand> comGet,
            Provider<CreateFolderCommand> comFolder,
            Provider<CreateUserCommand> comUser,
            Provider<CreateLockCommand> comLock,
            Provider<GetByNameCommand> comNameGet,
            Provider<SetImageCommand> comSetImage,
            Provider<GetRootPathCommand> comRootPath,
            GsonProvider gsonProvider ) {
        for (Provider<? extends LgoCommand<?, ?>> command : // need to move this into a properties file
                Arrays.asList(
                comHelp, comXml, comBrowse, comDelete, comLs, comGet,
                comFolder, comUser, comLock, comNameGet, comSetImage,
                comRootPath)) {
            commandMgr.setCommand(helpMgr, command);
        }
        gsonProvider.registerSerializer(SimpleAssetListBuilder.AssetList.class,
                new SimpleAssetListBuilder.GsonSerializer()
                );
    }

    /** NOOP */
    @Override
    public void start(BundleContext bc) throws Exception {

    }

    /** NOOP */
    @Override
    public void stop(BundleContext bc) throws Exception {
        
    }

}
