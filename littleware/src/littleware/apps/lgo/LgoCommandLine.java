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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.List;
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.client.UiFeedback;
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
     * which command to launch, process arguments up until "--",
     * then pass pass the remaining args as a single space-separated string
     * to the command.runCommand method.
     * 
     * @param v_args command-line args
     */
    public static void main( String[] v_args ) {
        try { 
            Injector     injector = Guice.createInjector( new Module[] {
                            new EzModule(),
                            new littleware.apps.swingclient.StandardSwingGuice(),
                            new littleware.apps.client.StandardClientGuice(),
                            new littleware.apps.misc.StandardMiscGuice(),
                            new littleware.security.auth.ClientServiceGuice(),
                            new PropertiesGuice( littleware.base.PropertiesLoader.get().loadProperties() )
                        }
            );
            LgoCommandDictionary m_command = injector.getInstance( LgoCommandDictionary.class );
            LgoHelpLoader        m_help = injector.getInstance( LgoHelpLoader.class );
            UiFeedback           feedback = new LoggerUiFeedback();

            for( LgoCommand cmd_register :
                // need to move this into a properties file
                new LgoCommand[] {
                    injector.getInstance( EzHelpCommand.class ),
                    injector.getInstance( XmlEncodeCommand.class ),
                    injector.getInstance( LgoBrowserCommand.class )
                }
                ) {

                m_command.setCommand( cmd_register.getName(), cmd_register );

                LgoHelp help = m_help.loadHelp( cmd_register.getName () );
                if ( null != help ) {
                    for( String s_alias : help.getShortNames() ) {
                        m_command.setCommand( s_alias, cmd_register );
                    }            
                } else {
                    olog.log ( Level.FINE, "No help available for command: " + cmd_register.getName() );
                }
            }
            if ( v_args.length == 0 ) { // launch help command by default
                System.out.print( m_command.getCommand( "help" ).runCommand( feedback, "help" ).toString() );
                return;
            }
            LgoCommand<?, ?> command = m_command.getCommand(v_args[0]); // injector.getInstance( LgoBrowserCommand.class ); //m_command.getCommand( LgoBrowserCommand.class.getName () );
            if (null == command) {
                System.out.print(m_command.getCommand("help").runCommand(feedback, "help").toString());
                return;
            }
            List<String> v_process = new ArrayList<String>();
            StringBuilder sb_in = new StringBuilder();

            for (int i = 1; i < v_args.length; ++i) {
                String s_arg = v_args[i];
                if (s_arg.equals("--")) {
                    // everything after -- goes into runCommand
                    for (int j = i + 1; j < v_args.length; ++j) {
                        sb_in.append(v_args[j]).append(" ");
                    // Note: trim() sb_in.toString before passing to run
                    }
                    break;
                }
                v_process.add(s_arg);
            }
            System.out.println(command.runCommand(feedback, sb_in.toString().trim()).toString());
        } catch ( Exception e ) {
            olog.log( Level.SEVERE, "Failed command, caught: " + e, e );
        }
    }
}
