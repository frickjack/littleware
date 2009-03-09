/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import littleware.apps.client.UiFeedback;
import littleware.base.AssertionFailedException;


/**
 * Handy LgoCommand baseclass
 */
public abstract class AbstractLgoCommand<Tin,Tout> implements LgoCommand<Tin,Tout> {
    /**
     * Subtypes must initialize the name property.
     * 
     * @param s_name
     */
    protected AbstractLgoCommand( String s_name ) {
        os_name = s_name;
    }
            
    private final String os_name;
    @Override
    public String getName() {
        return os_name;
    }


    private List<String>  ovArgs = Collections.emptyList();

    /** Just copy args into an internal list available to subtypes via getArgs */
    @Override
    public void processArgs( List<String> vArgs) throws LgoException {
        List<String> vCopy = new ArrayList<String>();
        vCopy.addAll( vArgs );
        ovArgs = Collections.unmodifiableList( vCopy );
    }

    /** Return unmodifiable copy of list passed to last call to processArgs */
    protected List<String> getArgs() { return ovArgs; }


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
     * @return argname.toLowerCase to argvalue map initialized by mapDefaults entries with non-null values
     * @throws LgoException if illegal vArgs detected
     */
    public static Map<String, String> processArgs( Map<String,String> mapDefaults, List<String> vArgs ) throws LgoException {
        final Map<String, String> mapResult = new HashMap<String,String>();

        for ( Map.Entry<String,String> entry : mapDefaults.entrySet() ) {
            mapResult.put( entry.getKey().toLowerCase(), entry.getValue() );
        }

        String sLastOption = null;
        for (String sArg : vArgs) {
            if (sArg.startsWith("-") || (sLastOption == null) ) {
                String sClean = sArg.replaceAll("^-+", "").toLowerCase();
                boolean bFound = false;
                if ( ! mapResult.containsKey( sClean ) ) {
                    throw new LgoArgException("Unable to process argument: " + sArg);
                }
                sLastOption = sClean;
                mapResult.put( sClean, "" );
            } else {
                mapResult.put( sLastOption, sArg);
                sLastOption = null;
            }
        }
        return mapResult;
    }


    /**
     * Provides a brain-dead implementation that just reads
     * in up to 1024 UTF-8 encoded characters, and calls
     * through to stream_out.write( runCommand(string).toString ).
     * 
     * @param streamin
     * @param streamout
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public void runCommand( UiFeedback feedback, InputStream streamin, OutputStream streamout
            ) throws LgoException, IOException
    {        
        char[] v_buffer = new char[1024];
        Reader reader = new InputStreamReader( streamin );
        Writer writer = new OutputStreamWriter( streamout, Charset.forName( "UTF-8" ) );
        
        for( int i_read = reader.read( v_buffer );
            i_read >= 0;
            i_read = reader.read( v_buffer )
            ) {
            writer.write( runCommand( feedback, new String( v_buffer, 0, i_read ) ).toString() );
        }
    }
            

    /**
     * Abstract implementation just calls through to
     *     runDynamic( s_in ).toString()
     * Override if that is not appropriate for your command.
     * 
     * @param s_in
     * @return
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public Tout runCommand( UiFeedback feedback, String s_in ) throws LgoException
    {
        return runDynamic( feedback, s_in );
    }

    /** Just calls runCommand().toString() */
    @Override
    public String runCommandLine( UiFeedback feedback, String s_in ) throws LgoException {
        Object result = runCommand( feedback, s_in );
        return (null == result) ? null : result.toString ();
    }

    /** Just does a direct cast (Tin) x_in */
    @Override
    public Tout runDynamic ( UiFeedback feedback, Object x_in ) throws LgoException {
        return runSafe( feedback, (Tin) x_in );
    }
    
    @Override
    public abstract Tout runSafe( UiFeedback feedback, Tin in ) throws LgoException;
    
    /** Subtypes should override via normal clone() rules */
    @Override
    public AbstractLgoCommand clone () {
        try {
            return (AbstractLgoCommand) super.clone();
        } catch ( CloneNotSupportedException e ) {
            throw new AssertionFailedException( "Clone whatever ?", e );
        }
    }
}
