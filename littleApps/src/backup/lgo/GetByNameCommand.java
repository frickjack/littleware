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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.feedback.Feedback;


public class GetByNameCommand extends AbstractAssetCommand<String,Asset> {
    private static final Logger log = Logger.getLogger( GetByNameCommand.class.getName() );
    private final AssetSearchManager osearch;


    @Inject
    public GetByNameCommand(
            HumanPicklerProvider        providePickler,
            AssetSearchManager          search
            ) {
        super( GetByNameCommand.class.getName(), providePickler, null );
        osearch = search;
    }

    private enum Option { name, type };

    @Override
    public Asset runCommand(Feedback feedback ) throws LgoException {
        final Map<String,String> mapDefault = new HashMap<String,String>();
        mapDefault.put( Option.name.toString(), "");
        mapDefault.put( Option.type.toString(), AssetType.LOCK.toString() );
        final Map<String,String> mapArg = null; //processArgs( mapDefault, getArgs() );
        final String sName = mapArg.get( Option.name.toString() );
        final String sType = mapArg.get( Option.type.toString() ).toLowerCase();
        AssetType  type = AssetType.UNKNOWN;
        for( AssetType possible : AssetType.getMembers() ) {
            String sPossible = possible.toString().toLowerCase();
            log.log( Level.FINE, "Scanning type argument {0} ?= {1}", new Object[]{sType, sPossible});
            if ( sType.equals( sPossible )
                    || (
                    sPossible.endsWith(sType)
                    && possible.isNameUnique()
                    )
                    ) {
                type = possible;
                break;
            }
        }
        if ( ! type.isNameUnique() ) {
            throw new LgoArgException( "Type is not name unique: " + type );
        }
        try {
            return osearch.getByName(sName, type).get();
        } catch ( Exception ex ) {
            throw new LgoArgException( "Failed to retrieve asset with name " + sName +
                    ", type " + type, ex );
        }
    }

}
