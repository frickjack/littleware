/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

/**
 * Command-line based lgo launcher.
 * Lgo is just a stupid shell for launching
 * other commands as an alternative to
 * setting up .jar files or shell scripts
 * for launching each command.  Typical syntax is:
 *     lgo command-name command-args
 */
public class LgoCommandLine {
    
    
    /**
     * Look at the first argument to determine 
     * which command to launch
     * 
     * @param v_args command-line args
     */
    public static void main( String[] v_args ) {
        Injector             injector = Guice.createInjector( new EzModule() );
        LgoCommandDictionary m_command = injector.getInstance( LgoCommandDictionary.class );
        LgoHelpLoader        m_help = injector.getInstance( LgoHelpLoader.class );
        
        for( LgoCommand cmd_register : 
            new LgoCommand[] {
                //new StdoutHelpCommand( m_command, m_help ),
                new XmlEncodeCommand ()
            }
            ) {
            LgoHelp help = m_help.loadHelp( cmd_register.getName () );
            m_command.setCommand( cmd_register.getName(), cmd_register );
            for( String s_alias : help.getShortNames() ) {
                m_command.setCommand( s_alias, cmd_register );
            }            
        }
    }

}
