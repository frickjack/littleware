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
import com.google.inject.Provider;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.Set;
import littleware.base.Whatever;
import littleware.base.XmlResourceBundle;
import littleware.base.feedback.Feedback;

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
    public LgoHelp runSafe( Feedback feedback, String sTargetIn ) {
        String sTarget = sTargetIn;
        if ( ((null == sTarget) || (sTarget.length() == 0))
                && (getArgs().size() > 0)
                )
        {
            sTarget = getArgs().get( getArgs().size() - 1 );
        }
        final LgoCommand<?,?> command = om_command.buildCommand( sTarget );
        if ( null == command ) {
            StringBuilder sbCommands = new StringBuilder();
            sbCommands.append( "No help found for command: " ).append( sTarget ).append(", available commands:" ).
                    append( Whatever.NEWLINE );
            final Set<String> vAlready = new HashSet<String>();
            for ( Provider<? extends LgoCommand<?,?>> provider : om_command.getCommands() ) {
                final LgoCommand comIndex = provider.get();
                if ( vAlready.contains( comIndex.getName() ) ) {
                    continue;
                }
                vAlready.add( comIndex.getName() );
                sbCommands.append( "    " ).append( comIndex.getName() );
                LgoHelp  help = om_help.loadHelp( comIndex.getName() );
                if ( null != help ) {
                    sbCommands.append( " [");
                    boolean bFirst = true;
                    for ( String sAlias : help.getShortNames() ) {
                        if ( ! bFirst ) {
                            sbCommands.append( ", " );
                        } else {
                            bFirst = false;
                        }
                        sbCommands.append(sAlias);
                    }
                    sbCommands.append( "] " ).append( help.getSynopsis() );
                }
                sbCommands.append( Whatever.NEWLINE );
            }
            final List<String> vNoAlias = Collections.emptyList();
            final List<LgoExample> vNoExample = Collections.emptyList();
            LgoHelp help = new EzLgoHelp( "command.not.found",
                    vNoAlias,
                    sbCommands.toString(),
                    "",
                    vNoExample
                    );
            return help;
        } else {
            LgoHelp help = om_help.loadHelp( command.getName (), olocale );
            if ( null != help ) {
                return help;
            }
            final List<String> vNoAlias = Collections.emptyList();
            final List<LgoExample> vNoExample = Collections.emptyList();

            return new EzLgoHelp( command.getName(), vNoAlias, "no help available for command", "", vNoExample );
        }
    }

    /**
     * Assemble the help information for
     * the given arguments, and return an
     * info string appropriate for the given locale.
     */
    protected String getHelpString ( Feedback feedback, Locale locale ) {
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
            // No help found - give the user a command-list
            String s_nohelp = MyResource.MissingHelp.getValue( bundle_help );
            StringBuilder  sb_help = new StringBuilder( 1000 );
            sb_help.append( s_nohelp ).append( Whatever.NEWLINE );
            sb_help.append( MyResource.CommandListIntro.getValue( bundle_help ) )
                    .append( Whatever.NEWLINE ).append( Whatever.NEWLINE );

            final Set<String>  vAlready = new HashSet<String> ();  // avoid duplicate entries (same command, different alias)
            for( Provider<? extends LgoCommand<?,?>> provider : om_command.getCommands () ) {
                final LgoCommand<?,?> command = provider.get();
                if ( vAlready.contains( command.getName() ) ) {
                    continue;
                }
                vAlready.add( command.getName() );
                help = om_help.loadHelp( command.getName (), locale );

                sb_help.append( command.getName () );
                if ( null != help ) {
                    sb_help.append( "( " );
                    boolean bFirst = true;
                    for( String s_alias: help.getShortNames() ) {
                        if ( ! bFirst ) {
                            sb_help.append( ", " );
                        } else {
                            bFirst = false;
                        }
                        sb_help.append( s_alias );
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
