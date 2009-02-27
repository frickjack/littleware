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

import com.google.inject.Inject;
import java.util.Locale;
import java.util.ResourceBundle;

import littleware.apps.client.UiFeedback;
import littleware.base.Whatever;
import littleware.base.XmlResourceBundle;

/**
 * Simple help-command baseclass just builds up
 * help-string in standard format for subclasses
 * that can send the info to whatever destination.
 *
 * @TODO process args to set Locale property
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
        super( "littleware.apps.lgo.EzHelpCommand" );
        om_command = m_command;
        om_help = m_help;
    }
    
    
    private Locale  olocale = Locale.getDefault();
    
    /**
     * Property tracks which locale to prevent help in
     */
    public Locale getLocale () { return olocale; }
    public void setLocale ( Locale locale ) {
        olocale = locale;
    }

    @Override
    public LgoHelp runSafe( UiFeedback feedback, String s_target ) {
        LgoCommand<?,?> command = om_command.getCommand( s_target );
        if ( (null == command) ) {
            // try again
            if( ! getArgs().isEmpty() ) {
                command = om_command.getCommand( getArgs().get(0) );
            }
            if ( null == command ) {
                return null;
            }
        }
        return om_help.loadHelp( command.getName (), olocale );
    }

    /**
     * Assemble the help information for
     * the given arguments, and return an
     * info string appropriate for the given locale.
     */
    protected String getHelpString ( UiFeedback feedback, Locale locale ) {
        ResourceBundle bundle_help = XmlResourceBundle.getBundle( EzHelpCommand.class.getName() + "Resources",
                locale
                );
        LgoHelp  help = runSafe( feedback, null );
        
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
    
}
