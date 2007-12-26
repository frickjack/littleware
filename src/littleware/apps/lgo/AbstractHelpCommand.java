/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.util.Locale;
import java.util.ResourceBundle;

import littleware.base.Whatever;
import littleware.base.XmlResourceBundle;

/**
 * Simple help-command baseclass just builds up
 * help-string in standard format for subclasses
 * that can send the info to whatever destination.
 */
public abstract class AbstractHelpCommand extends AbstractLgoCommand {

    private final LgoHelpLoader          om_help;
    private final LgoCommandDictionary   om_command;
    
    /**
     * Resources we expect to find in the HelpCommandResources
     * ResourceBundle.
     */
    private enum MyResource {
        Name, Alias, Synopsis, Description, Example,
        MissingHelp, NoSuchCommand, CommandListIntro
                ;
        
        /**
         * Just little convenience - looks up this resource
         * in the given ResourceBundle.
         * 
         * @param bundle to look into
         * @return value that goes with this as a key
         */
        public String getValue( ResourceBundle bundle ) {
            return bundle.getString( this.toString () );
        }
    }
    
    
    /**
     * Inject sources for help and command data
     * 
     * @param m_help
     * @param m_command
     */
    public AbstractHelpCommand( LgoHelpLoader m_help,
            LgoCommandDictionary m_command
            ) 
    {
        super( "littleware.apps.lgo.HelpCommand" );
        om_command = m_command;
        om_help = m_help;
    }
    
    /**
     * Assemble the help information for
     * the given arguments, and return an
     * info string appropriate for the given locale.
     */
    protected String getHelpString ( Locale locale ) {
        String[]        v_argv = this.getCommandArgs ();
        ResourceBundle  bundle_help = XmlResourceBundle.getXmlBundle( getName() + "Resources" );
        
        if ( v_argv.length > 0 ) {
            LgoCommand command = om_command.getCommand( v_argv[0] );
            if ( null != command ) {
                LgoHelp help = om_help.loadHelp( command.getName (), locale );
                if ( null != help ) {
                    StringBuilder  sb_help = new StringBuilder( 1000 );

                    sb_help.append( MyResource.Name.getValue(bundle_help) )
                            .append( Whatever.NEWLINE).append( "    " )
                            .append( help.getFullName() )
                            .append( Whatever.NEWLINE).append( Whatever.NEWLINE);

                    sb_help.append( MyResource.Alias.getValue( bundle_help ) )
                            .append( Whatever.NEWLINE).append( "    " );
                    for ( String s_alias : help.getShortNames() ) {
                       sb_help.append( s_alias ).append( ", " );
                    }
                    sb_help.append( Whatever.NEWLINE).append( "    " );

                    sb_help.append( MyResource.Synopsis.getValue(bundle_help))
                            .append( Whatever.NEWLINE).append( "    " )
                            .append( help.getSynopsis() )
                            .append( Whatever.NEWLINE).append( Whatever.NEWLINE );

                    sb_help.append( MyResource.Description.getValue(bundle_help))
                            .append( Whatever.NEWLINE).append( "    " )
                            .append( help.getDescription() )
                            .append( Whatever.NEWLINE).append( Whatever.NEWLINE );

                    sb_help.append( MyResource.Example.getValue(bundle_help));
                    for( LgoExample example : help.getExamples() ) {
                        sb_help.append( Whatever.NEWLINE).append( "    " )
                                .append( example.getTitle () )
                                .append( Whatever.NEWLINE).append( "    " )
                                .append( example.getDescription() )
                                .append( Whatever.NEWLINE).append( Whatever.NEWLINE );
                    } 
                    return sb_help.toString ();
                }
                return MyResource.MissingHelp.getValue( bundle_help );
            }
            return MyResource.NoSuchCommand.getValue( bundle_help );
        } else {                
            StringBuilder  sb_help = new StringBuilder( 1000 );
            sb_help.append( MyResource.CommandListIntro.getValue( bundle_help ) )
                    .append( Whatever.NEWLINE ).append( Whatever.NEWLINE );

            for( LgoCommand command : om_command.getCommands () ) {
                LgoHelp help = om_help.loadHelp( command.getName (), locale );
                
                sb_help.append( command.getName () );
                if ( null != help ) {
                    sb_help.append( "( " );
                    for( String s_alias: help.getShortNames() ) {
                        sb_help.append( s_alias ).append( ", " );
                    }
                    sb_help.append( ") " ).append( ":" )
                            .append( help.getShortNames() );
                } else {
                    sb_help.append( ":" ).append( MyResource.MissingHelp.getValue( bundle_help ) );
                }
                sb_help.append( Whatever.NEWLINE );
            }
            return sb_help.toString ();
        }        
    }
    
    public abstract void runCommand();
}
