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

import java.util.ResourceBundle;
import java.util.logging.Logger;

//import gnu.getopt.Getopt;
//import gnu.getopt.LongOpt;

import java.util.List;
import littleware.apps.client.Feedback;
import littleware.base.AssertionFailedException;
import littleware.base.XmlSpecial;


/**
 * XML encode/decode LgoCommand
 */
public class XmlEncodeCommand extends AbstractLgoCommand<String,String> {
    private static final Logger olog = Logger.getLogger( XmlEncodeCommand.class.getName () );
    
    /**
     * Constructor just sets the command-name to this.class.getName,
     * and the command-help property to the supplied help object.
     */
    public XmlEncodeCommand() {
        super( XmlEncodeCommand.class.getName() );        
    }
    
    public enum SubCommand {
        encode, decode, version, help;
    }
    
    private SubCommand on_subcommand = SubCommand.encode;
    
    /**
     * Subcommand property - may also be set via processArgs
     */
    public SubCommand getSubCommand () { 
        return on_subcommand;
    }
    public void setSubCommand( SubCommand n_subcommand ) {
        on_subcommand = n_subcommand;
    }

    /*..
    private static ResourceBundle obundle_support = null;

    private static LongOpt[]      ov_longopts = new LongOpt[] {
                new LongOpt(obundle_support.getString("version.option"), LongOpt.NO_ARGUMENT, null, 1),
                new LongOpt(obundle_support.getString("encode.option"), LongOpt.NO_ARGUMENT, null, 'e'),
                new LongOpt(obundle_support.getString("decode.option"), LongOpt.NO_ARGUMENT, null, 'd')
            };
*/
    
    /**
     * Reset the XmlEncodeCommand properties based on the
     * command-line arguments.
     * 
     * @param vArgs
     */
    @Override
    public void processArgs( List<String> argv ) throws LgoException {
        if ( argv.isEmpty()  ) {
            return;
        }
        final String sArg = argv.get(0).trim().replaceAll( "-", "" );
        if ( sArg.toLowerCase().startsWith( "d" ) ) {
            on_subcommand = SubCommand.decode;
        } else if ( sArg.startsWith("v" ) ) {
            on_subcommand = SubCommand.version;
        }
        /*... Remove gnu getopt dependency unless we ant to use it more widely ...
        if ( null == obundle_support ) {
            obundle_support = ResourceBundle.getBundle( XmlEncodeCommand.class.getName() + "Resources" );
        }
        final Getopt opts = new Getopt(obundle_support.getString("appname"), 
                v_argv.toArray( new String[ v_argv.size() ] ), "ed", ov_longopts
                );


        for (int i_opt = opts.getopt();
                i_opt != -1;
                i_opt = opts.getopt()) {
            switch (i_opt) {
                case 1:
                    on_subcommand = SubCommand.version;
                    break;
                case 'd':
                    on_subcommand = SubCommand.encode;
                    break;
                case 'e':
                    on_subcommand = SubCommand.encode;
                    break;
                default: 
                    throw new LgoBadArgumentException( "Illegal option, try -h for help" );
            }
        }
         */
    }            
    
    
    

    @Override
    public String runCommand( Feedback feedback, String s_in ) {
        // Need to add ResourceBundle stuff ...
        switch( on_subcommand ) {
            case version:
                return this.getClass().getName () + " version 0.0";
            case encode:
                return XmlSpecial.encode(s_in);
            case decode:
                return XmlSpecial.decode(s_in);
            default:
                throw new AssertionFailedException( "Should not reach this state" );
        }
    }

    @Override
    public String runSafe( Feedback feedback, String s_in ) {
        return runCommand( feedback, s_in );
    }
}
