/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.lgo;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.Set;
import littleware.base.Option;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Simple help-command baseclass just builds up
 * help-string in standard format for subclasses
 * that can send the info to whatever destination.
 *
 * @TODO process args to set Locale property
 */
public class EzHelpCommand extends AbstractLgoBuilder<String> {

    private final LgoHelpLoader om_help;
    private final LgoCommandDictionary om_command;

    /**
     * Inject sources for help and command data
     *
     * @param m_help
     * @param m_command
     */
    @Inject
    public EzHelpCommand(LgoHelpLoader m_help,
            LgoCommandDictionary m_command) {
        super( EzHelpCommand.class.getName() );
        this.om_command = m_command;
        this.om_help = m_help;
    }

    @Override
    public LgoCommand buildSafe(String input) {
        return new HelpCommand(input);
    }

    @Override
    public LgoCommand buildFromArgs(List<String> args) {
        if( args.isEmpty() ) {
            return buildSafe( "" );
        } else {
            return buildSafe( args.get(0) );
        }
    }

    /**
     * Resources we expect to find in the HelpCommandResources
     * ResourceBundle.
     */
    private enum MyResource {

        Name, Alias, Synopsis, Description, Example,
        MissingHelp, NoSuchCommand, CommandListIntro;

        /**
         * Just little convenience - looks up this resource
         * in the given ResourceBundle.
         *
         * @param bundle to look into
         * @return value that goes with this as a key
         */
        public String getValue(ResourceBundle bundle) {
            return bundle.getString(this.toString());
        }
    }

    private class HelpCommand extends AbstractLgoCommand<String, LgoHelp> {

        public HelpCommand(String input) {
            super(EzHelpCommand.class.getName(), input);
        }
        private Locale olocale = Locale.getDefault();

        /**
         * Property tracks which locale to present help in
         */
        public Locale getLocale() {
            return olocale;
        }

        public void setLocale(Locale locale) {
            olocale = locale;
        }

        @Override
        public LgoHelp runCommand(Feedback feedback) {
            String sTarget = getInput();
            final Option<LgoCommand.LgoBuilder> maybe = om_command.buildCommand(sTarget);
            if ( maybe.isEmpty() ) {
                StringBuilder sbCommands = new StringBuilder();
                sbCommands.append("No help found for command: ").append(sTarget).append(", available commands:").
                        append(Whatever.NEWLINE);
                final Set<String> vAlready = new HashSet<String>();
                for (Provider<LgoCommand.LgoBuilder> provider : om_command.getCommands()) {
                    final LgoCommand.LgoBuilder comIndex = provider.get();
                    if (vAlready.contains(comIndex.getName())) {
                        continue;
                    }
                    vAlready.add(comIndex.getName());
                    sbCommands.append("    ").append(comIndex.getName());
                    LgoHelp help = om_help.loadHelp(comIndex.getName());
                    if (null != help) {
                        sbCommands.append(" [");
                        boolean bFirst = true;
                        for (String sAlias : help.getShortNames()) {
                            if (!bFirst) {
                                sbCommands.append(", ");
                            } else {
                                bFirst = false;
                            }
                            sbCommands.append(sAlias);
                        }
                        sbCommands.append("] ").append(help.getSynopsis());
                    }
                    sbCommands.append(Whatever.NEWLINE);
                }
                final List<String> vNoAlias = Collections.emptyList();
                final List<LgoExample> vNoExample = Collections.emptyList();
                LgoHelp help = new EzLgoHelp("command.not.found",
                        vNoAlias,
                        sbCommands.toString(),
                        "",
                        vNoExample);
                return help;
            } else {
                final String name = maybe.get().getName();
                final LgoHelp help = om_help.loadHelp(name, olocale);
                if (null != help) {
                    return help;
                }
                final List<String> vNoAlias = Collections.emptyList();
                final List<LgoExample> vNoExample = Collections.emptyList();

                return new EzLgoHelp(name, vNoAlias, "no help available for command", "", vNoExample);
            }
        }
    }
}
