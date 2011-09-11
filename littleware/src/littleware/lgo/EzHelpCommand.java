/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
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

    private final LgoHelpLoader helpLoader;
    private final LgoCommandDictionary commandDictionary;

    /**
     * Inject sources for help and command data
     *
     * @param helpLoader
     * @param commandDictionary
     */
    @Inject
    public EzHelpCommand(LgoHelpLoader helpLoader,
            LgoCommandDictionary commandDictionary) {
        super(EzHelpCommand.class.getName());
        this.commandDictionary = commandDictionary;
        this.helpLoader = helpLoader;
    }

    @Override
    public LgoCommand buildSafe(String input) {
        return new HelpCommand(input);
    }

    @Override
    public LgoCommand buildFromArgs(List<String> args) {
        if (args.isEmpty()) {
            return buildSafe("");
        } else {
            return buildSafe(args.get(0));
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
        
        private final List<String> emptyAliasList = Collections.emptyList();
        private final List<LgoExample> emptyExampleList = Collections.emptyList();

        @Override
        public LgoHelp runCommand(Feedback feedback) {
            final String targetCommand = getInput();
            final Option<LgoCommand.LgoBuilder> maybe = commandDictionary.buildCommand(targetCommand);
            if (maybe.isEmpty()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("No help found for command: ").append(targetCommand).append(", available commands:").
                        append(Whatever.NEWLINE);
                final Set<String> commandNames = new HashSet<String>();
                for (Provider<LgoCommand.LgoBuilder> provider : commandDictionary.getCommands()) {
                    final LgoCommand.LgoBuilder comIndex = provider.get();
                    if (commandNames.contains(comIndex.getName())) {
                        continue;
                    }
                    commandNames.add(comIndex.getName());
                    sb.append("    ").append(comIndex.getName());
                    for( LgoHelp help : helpLoader.loadHelp(comIndex.getName()) ) {
                        sb.append(" [");
                        boolean bFirst = true;
                        for (String sAlias : help.getShortNames()) {
                            if (!bFirst) {
                                sb.append(", ");
                            } else {
                                bFirst = false;
                            }
                            sb.append(sAlias);
                        }
                        sb.append("] ").append(help.getSynopsis());
                    }
                    sb.append(Whatever.NEWLINE);
                }
                LgoHelp help = new EzLgoHelp("command.not.found",
                        emptyAliasList,
                        sb.toString(),
                        "",
                        emptyExampleList);
                return help;
            } else {
                final String name = maybe.get().getName();
                return helpLoader.loadHelp(name, olocale).getOr(
                        new EzLgoHelp(name, emptyAliasList, "no help available for command", "", emptyExampleList)
                        );
            }
        }
    }
}
