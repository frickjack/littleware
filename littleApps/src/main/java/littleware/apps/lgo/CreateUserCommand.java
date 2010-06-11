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

import littleware.lgo.LgoArgException;
import littleware.lgo.LgoException;
import com.google.inject.Inject;
import java.util.Map;
import java.util.logging.Level;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.LittleUser;
import littleware.security.SecurityAssetType;

/**
 * Create a new littleware.USER asset at /littleware.home/Users/name.
 * Return the new user's objectid.
 */
public class CreateUserCommand extends AbstractAssetCommand<String,LittleUser> {
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
    public LittleUser runSafe(Feedback feedback, String sDefaultName ) throws LgoException {
        final Map<String,String> mapArg = processArgs( getArgs(),
                Option.name.toString(), Option.admin.toString()
                );
        String sName = mapArg.get( Option.name.toString() );
        if ( Whatever.get().empty( sName ) ) {
            sName = sDefaultName;
        }
        if ( Whatever.get().empty( sName ) ) {
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
        final LittleUser userNew;
        try {
            userNew = omgrAsset.saveAsset( 
                    SecurityAssetType.USER.create().parent( aFolder ).name( sName ).build()
                    , "CreateUserCommand" ).narrow();
        } catch ( Exception ex ) {
            throw new LgoException( "Failed to create new user: " + sName, ex );
        }

        final boolean bAdmin = ! Whatever.get().empty( mapArg.get( Option.admin.toString() ) );
        if ( bAdmin ) {
            try {
                final LittleGroup groupAdmin = osearch.getByName(
                        AccountManager.LITTLEWARE_ADMIN_GROUP,
                        SecurityAssetType.GROUP
                        ).get().narrow();
                omgrAsset.saveAsset( groupAdmin.copy().add(userNew).build()
                        , "Added user " + sName );
            } catch ( Exception ex ) {
                feedback.log( Level.SEVERE,
                        "Failed to add new user " + sName + " to admin group"
                        );
            }
        }
        return userNew;  
    }

}
