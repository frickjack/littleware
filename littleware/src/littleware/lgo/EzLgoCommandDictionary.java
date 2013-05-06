/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.lgo;

import com.google.inject.Provider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.lgo.LgoCommand.LgoBuilder;

/**
 * Simple implementation of LgoCommandDictionary
 */
public class EzLgoCommandDictionary implements LgoCommandDictionary {
    private static final Logger   log = Logger.getLogger( EzLgoCommandDictionary.class.getName() );
    private final Map<String, Provider<LgoCommand.LgoBuilder>> commandMap = new HashMap<>();

    @Override
    public Collection<LgoCommand.LgoBuilder> guessCommand(String s_partial) {
        final Option<LgoCommand.LgoBuilder> maybe = buildCommand(s_partial);

        if ( maybe.isEmpty() ) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.singletonList( maybe.get() );
        }
    }

    @Override
    public Option<LgoCommand.LgoBuilder> buildCommand(String s_name) {
        final Provider<LgoCommand.LgoBuilder> provider = commandMap.get(s_name);
        if ( null == provider ) {
            return Maybe.empty();
        } else {
            return Maybe.something( provider.get() );
        }
    }

    @Override
    public void setCommand(String s_name, Provider<? extends LgoCommand.LgoBuilder> provideCommand) {
        commandMap.put(s_name, (Provider<LgoBuilder>) provideCommand);
    }

    @Override
    public Collection<Provider<LgoCommand.LgoBuilder>> getCommands() {
        return commandMap.values();
    }

    @Override
    public void setCommand(LgoHelp help, Provider<? extends LgoCommand.LgoBuilder> provideCommand) {
        setCommand(help.getFullName(), provideCommand);
        for (String sName : help.getShortNames()) {
            setCommand(sName, provideCommand);
        }
    }

    @Override
    public Option<LgoHelp> setCommand(LgoHelpLoader mgrHelp, Provider<? extends LgoCommand.LgoBuilder> provideCommand) {
        final LgoCommand.LgoBuilder command = provideCommand.get();
        final Option<LgoHelp> help = mgrHelp.loadHelp( command.getName());
        if ( help.isSet() ) {
            this.setCommand(help.get(), provideCommand);
        } else {
            log.log(Level.FINE, "No help available for command: {0}", command.getName());
            this.setCommand(command.getName(), provideCommand);
        }
        return help;
    }

    @Override
    public Option<Provider<LgoCommand.LgoBuilder>> getProvider(String s_name) {
        return Maybe.something(commandMap.get( s_name ));
    }
}
