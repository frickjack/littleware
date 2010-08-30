/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractLgoBuilder<InType> implements LgoCommand.LgoBuilder {
    private static final Logger log = Logger.getLogger( AbstractLgoBuilder.class.getName() );
    private final String name;

    public AbstractLgoBuilder( String name ) {
        this.name = name;
    }
    @Override
    public String getName() { return  name; }

    @Override
    public final LgoCommand buildWithInput( Object input ) {
        return buildSafe( (InType) input );
    }

    public abstract LgoCommand buildSafe( InType input );

    /**
     * Bonehead argument parser expects args in form
     * -*argname1 arg1value -*argname2 arg2value ...
     * , except in case -+argname1 -+argname2 just
     * sets argname1 to empty string in result map
     * to allow for boolean flag detection.
     * Caller needs to scan the result to verify presence
     * of required arguments or whatever.
     * Converts argnames to lowercase.
     *
     * @param mapDefaults read-only holds default values
     *                    for every arg.  Copies every entry
     *                    into the result map, then possibly
     *                    overwrites each entry by vArgs processing
     * @param vArgs to process
     * @return argname.toLowerCase to argvalue map initialized by mapDefaults entries
     * @throws IllegalArgumentException on processing failure
     */
    public static Map<String, String> processArgs( List<String> vArgs, Map<String,String> mapDefaults ) {
        final Map<String, String> mapResult = new HashMap<String,String>();

        for ( Map.Entry<String,String> entry : mapDefaults.entrySet() ) {
            log.log( Level.FINE, "Placing default {0} -to- {1}", new Object[]{entry.getKey(), entry.getValue()});
            mapResult.put( entry.getKey().toLowerCase(), entry.getValue() );
        }

        String sLastOption = null;
        for (String sArg : vArgs) {
            if ( null == sArg ) {
                continue;
            }
            log.log( Level.FINE, "Processing arg: {0}", sArg);
            if (sArg.startsWith("-") || (sLastOption == null) ) {
                String sClean = sArg.trim().replaceAll("^-+", "").toLowerCase();
                if ( sClean.length() == 0 ) {
                    continue;
                }

                boolean bFound = mapResult.containsKey( sClean );
                if ( ! bFound ) {
                    // See if any keys start with sClean
                    for ( String sOpt: mapResult.keySet() ) {
                        if ( sOpt.startsWith( sClean ) ) {
                            sClean = sOpt;
                            bFound = true;
                            break;
                        }
                    }
                }
                if ( ! bFound ) {
                    throw new IllegalArgumentException("Unable to process argument: " + sArg);
                }
                sLastOption = sClean;
                log.log( Level.FINE, "Set {0} -to- empty", sClean);
                mapResult.put( sClean, "" );
            } else {
                mapResult.put( sLastOption, sArg);
                log.log( Level.FINE, "Set {0} -to- {1}", new Object[]{sLastOption, sArg});
                sLastOption = null;
            }
        }
        return mapResult;
    }

    /**
     * Just calls through to processArgs( mapOption, vArgs ) where
     * vOptions specify the option keys each with null default value
     *
     * @param vArgs
     * @param vOption possible command-line options (path, type, whatever)
     * @return processed arguments
     * @throws littleware.apps.lgo.LgoArgException
     */
    public static Map<String, String> processArgs( List<String> vArgs, String ... vOption ) {
        final Map<String,String> mapOption = new HashMap<String,String>();
        for ( String sOption : vOption ) {
            mapOption.put( sOption, null );
        }
        return processArgs( vArgs, mapOption );
    }

}
