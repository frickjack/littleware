/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

import littleware.asset.Asset;
import littleware.base.UUIDFactory;
import littleware.security.auth.*;
import littleware.apps.swingclient.IconLibrary;

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
            //java.rmi.server.RMISocketFactory.setSocketFactory( new sun.rmi.transport.proxy.RMIHttpToCGISocketFactory() );
            String s_server = "littleware.frickjack.com"; 
            SessionManager m_session = SessionUtil.getSessionManager ( s_server, 1239 );
            SessionHelper helper = null;
            if ( v_args.length < 1 ) {
                olog.log( Level.SEVERE, "Usage: lgo <username> <password>" );
                olog.log( Level.SEVERE, "... or: lgo <session-id>");
                System.exit( 1 );
            } else if ( v_args.length == 1 ) {
                helper = m_session.getSessionHelper( UUIDFactory.parseUUID( v_args[0] ) );
            } else {
                helper = m_session.login( v_args[0], v_args[1], "LgoCommand login" );
            }
            Injector     injector = Guice.createInjector( new Module[] {
                            new EzModule(),
                            new littleware.apps.swingclient.StandardSwingGuice(),
                            new littleware.apps.client.StandardClientGuice(),
                            new littleware.security.auth.ClientServiceGuice( helper )
                        }
            );
            LgoCommandDictionary m_command = injector.getInstance( LgoCommandDictionary.class );
            LgoHelpLoader        m_help = injector.getInstance( LgoHelpLoader.class );

            injector.getInstance( IconLibrary.class ).setRoot( s_server + "/cmm/lib/icons" );
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
