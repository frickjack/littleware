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

import com.google.inject.Inject;
import java.util.Map;
import java.util.logging.Level;
import littleware.apps.client.UiFeedback;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.Whatever;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.LittleUser;
import littleware.security.SecurityAssetType;

/**
 * Create a new littleware.USER asset at /littleware.home/Users/name.
 * Return the new user's objectid.
 */
public class CreateUserCommand extends AbstractCreateCommand<String,LittleUser> {
    private final AssetSearchManager osearch;
    private final AssetManager omgrAsset;
    private final AssetPathFactory ofactoryPath;

    @Inject
    public CreateUserCommand( 
            AssetSearchManager search,
            AssetManager       mgrAsset,
            AssetPathFactory   factoryPath,
            HumanPicklerProvider providePickler
            ) {
        super( CreateUserCommand.class.getName(), providePickler );
        osearch = search;
        omgrAsset = mgrAsset;
        ofactoryPath = factoryPath;
    }

    private enum Option { name, admin };
    
    @Override
    public LittleUser runSafe(UiFeedback feedback, String sDefaultName ) throws LgoException {
        final Map<String,String> mapArg = processArgs( getArgs(),
                Option.name.toString(), Option.admin.toString()
                );
        String sName = mapArg.get( Option.name.toString() );
        if ( Whatever.empty( sName ) ) {
            sName = sDefaultName;
        }
        if ( Whatever.empty( sName ) ) {
            throw new LgoArgException ( "Required argument --name not set" );
        }
        final String sFolder = "/littleware.home/Users";
        final Asset aFolder;
        try {
            aFolder = osearch.getAssetAtPath(
                    ofactoryPath.createPath( sFolder )
                    ).get();
        } catch ( Exception ex ) {
            throw new LgoException( "Failed to load parent folder: " + sFolder, ex );
        }
        LittleUser userNew = AssetType.createSubfolder( SecurityAssetType.USER,
                sName, aFolder
                );
        try {
            userNew = omgrAsset.saveAsset( userNew, "CreateUserCommand" );
        } catch ( Exception ex ) {
            throw new LgoException( "Failed to create new user: " + sName, ex );
        }

        final boolean bAdmin = ! Whatever.empty( mapArg.get( Option.admin.toString() ) );
        if ( bAdmin ) {
            try {
                final LittleGroup groupAdmin = osearch.getByName(
                        AccountManager.LITTLEWARE_ADMIN_GROUP,
                        SecurityAssetType.GROUP
                        ).get();
                groupAdmin.addMember(userNew);
                omgrAsset.saveAsset( groupAdmin, "Added user " + sName );
            } catch ( Exception ex ) {
                feedback.log( Level.SEVERE,
                        "Failed to add new user " + sName + " to admin group"
                        );
            }
        }
        return userNew;  //.getObjectId();
    }

}
