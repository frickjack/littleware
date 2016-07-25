package littleware.lgo;

import com.google.inject.Provider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple implementation of LgoCommandDictionary
 */
public class EzLgoCommandDictionary implements LgoCommandDictionary {
    private static final Logger   log = Logger.getLogger( EzLgoCommandDictionary.class.getName() );
    private final Map<String, Provider<LgoCommand.LgoBuilder>> commandMap = new HashMap<>();

    @Override
    public Collection<LgoCommand.LgoBuilder> guessCommand(String s_partial) {
        final Optional<LgoCommand.LgoBuilder> maybe = buildCommand(s_partial);
        final Collection<LgoCommand.LgoBuilder> result;

        if ( ! maybe.isPresent() ) {
            result = Collections.emptyList();
        } else {
            result = Collections.singletonList( maybe.get() );
        }
        return result;
    }

    @Override
    public Optional<LgoCommand.LgoBuilder> buildCommand(String name) {
        final Provider<LgoCommand.LgoBuilder> provider = commandMap.get(name);
        if ( null == provider ) {
            return Optional.empty();
        } else {
            return Optional.ofNullable( provider.get() );
        }
    }

    @Override
    public void setCommand(String name, Provider<? extends LgoCommand.LgoBuilder> provideCommand) {
        commandMap.put(name, (Provider<LgoCommand.LgoBuilder>) provideCommand );
    }

    @Override
    public Collection<Provider<LgoCommand.LgoBuilder>> getCommands() {
        return commandMap.values();
    }

    @Override
    public void setCommand(LgoHelp help, Provider<? extends LgoCommand.LgoBuilder> provideCommand) {
        setCommand(help.getFullName(), provideCommand);
        for (String name : help.getShortNames()) {
            setCommand(name, provideCommand);
        }
    }

    @Override
    public Optional<LgoHelp> setCommand(LgoHelpLoader mgrHelp, Provider<? extends LgoCommand.LgoBuilder> provideCommand) {
        final LgoCommand.LgoBuilder command = provideCommand.get();
        final Optional<LgoHelp> help = mgrHelp.loadHelp( command.getName());
        if ( help.isPresent() ) {
            this.setCommand(help.get(), provideCommand);
        } else {
            log.log(Level.FINE, "No help available for command: {0}", command.getName());
            this.setCommand(command.getName(), provideCommand);
        }
        return help;
    }

    @Override
    public Optional<Provider<LgoCommand.LgoBuilder>> getProvider(String s_name) {
        return Optional.ofNullable(commandMap.get( s_name ));
    }
}
