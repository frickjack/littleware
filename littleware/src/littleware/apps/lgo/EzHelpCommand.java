/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import com.google.inject.Inject;
import java.util.Locale;
import java.util.ResourceBundle;

import littleware.base.Whatever;
import littleware.base.XmlResourceBundle;

/**
 * Simple help-command baseclass just builds up
 * help-string in standard format for subclasses
 * that can send the info to whatever destination.
 */
public class EzHelpCommand extends AbstractLgoCommand<String,LgoHelp> {

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
    @Inject
    public EzHelpCommand( LgoHelpLoader m_help,
            LgoCommandDictionary m_command
            ) 
    {
        super( "littleware.apps.lgo.HelpCommand" );
        om_command = m_command;
        om_help = m_help;
    }
    
    /** help with what ? */
    private String  os_target = "help";
    
    /**
     * Just sets the target property to v_args[0]
     * 
     * @param v_args
     */
    @Override
    public void processCommandArgs( String[] v_args ) {
        if ( v_args.length > 0 ) {
            os_target = v_args[0];
        }
    }
    
    /**
     * Property tracks the help-target
     * that the help command should present info on
     */
    public String getHelpTarget () {
        return os_target;
    }
    /**
     * Set the name of the command to get help on at
     * runCommand time.
     * 
     * @param s_target
     */
    public void setHelpTarget ( String s_target ) {
        os_target = s_target;
    }
    
    private Locale  olocale = Locale.getDefault();
    
    /**
     * Property tracks which locale to prevent help in
     */
    public Locale getLocale () { return olocale; }
    public void setLocale ( Locale locale ) {
        olocale = locale;
    }

    
    public LgoHelp runSafe( String s_ignore ) {                
        LgoCommand<?,?> command = om_command.getCommand( os_target );
        if ( null == command ) {
            return null;
        }
        return om_help.loadHelp( command.getName (), olocale );
    }

    /**
     * Assemble the help information for
     * the given arguments, and return an
     * info string appropriate for the given locale.
     */
    protected String getHelpString ( Locale locale ) { 
        ResourceBundle bundle_help = XmlResourceBundle.getBundle( EzHelpCommand.class.getName() + "Resources",
                locale
                );
        LgoHelp  help = runSafe( null );
        
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

        } else {
            String s_nohelp = MyResource.MissingHelp.getValue( bundle_help );
            StringBuilder  sb_help = new StringBuilder( 1000 );
            sb_help.append( s_nohelp ).append( Whatever.NEWLINE );
            sb_help.append( MyResource.CommandListIntro.getValue( bundle_help ) )
                    .append( Whatever.NEWLINE ).append( Whatever.NEWLINE );

            for( LgoCommand command : om_command.getCommands () ) {
                help = om_help.loadHelp( command.getName (), locale );

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
    
    @Override
    public EzHelpCommand clone () {
        return (EzHelpCommand) super.clone();

    }
    
    @Override
    public boolean equals( Object x ) {
        return ((null != x)
                && (x instanceof EzHelpCommand)
                && ((EzHelpCommand) x).getHelpTarget().equals( getHelpTarget() )
                && ((EzHelpCommand) x).getLocale().equals( getLocale() )
                );
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.os_target != null ? this.os_target.hashCode() : 0);
        hash = 53 * hash + (this.olocale != null ? this.olocale.hashCode() : 0);
        return hash;
    }
}
