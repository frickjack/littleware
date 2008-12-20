/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import littleware.base.PropertiesGuice;


/**
 * Command-line based lgo launcher.
 * Lgo is just a stupid shell for launching
 * other commands as an alternative to
 * setting up .jar files or shell scripts
 * for launching each command.  Typical syntax is:
 *     lgo command-name command-args
 */
public class LgoCommandLine {
    private static final Logger olog = Logger.getLogger( LgoCommandLine.class.getName () );
    
    /**
     * Look at the first argument to determine 
     * which command to launch
     * 
     * @param v_args command-line args
     */
    public static void main( String[] v_args ) {
        try { 
            Injector     injector = Guice.createInjector( new Module[] {
                            new EzModule(),
                            new littleware.apps.swingclient.StandardSwingGuice(),
                            new littleware.apps.client.StandardClientGuice(),
                            new littleware.security.auth.ClientServiceGuice(),
                            new PropertiesGuice( littleware.base.PropertiesLoader.get().loadProperties() )
                        }
            );
            LgoCommandDictionary m_command = injector.getInstance( LgoCommandDictionary.class );
            LgoHelpLoader        m_help = injector.getInstance( LgoHelpLoader.class );

            for( LgoCommand cmd_register : 
                new LgoCommand[] {
                    //new StdoutHelpCommand( m_command, m_help ),
                    injector.getInstance( XmlEncodeCommand.class ),
                    injector.getInstance( LgoBrowserCommand.class )
                }
                ) {
                LgoHelp help = m_help.loadHelp( cmd_register.getName () );
                if ( null != help ) {
                    m_command.setCommand( cmd_register.getName(), cmd_register );
                    for( String s_alias : help.getShortNames() ) {
                        m_command.setCommand( s_alias, cmd_register );
                    }            
                } else {
                    olog.log ( Level.FINE, "No help available for command: " + cmd_register.getName() );
                }
            }
            LgoCommand<?,?> command = m_command.getCommand( LgoBrowserCommand.class.getName () ); // injector.getInstance( LgoBrowserCommand.class ); //m_command.getCommand( LgoBrowserCommand.class.getName () );
            command.runDynamic( "/byname:littleware.home:type:littleware.HOME/" );
        } catch ( Exception e ) {
            olog.log( Level.SEVERE, "Failed command, caught: " + e, e );
        }
    }
}
