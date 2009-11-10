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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple implementation of LgoCommandDictionary
 */
public class EzLgoCommandDictionary implements LgoCommandDictionary {
    private static final Logger   olog = Logger.getLogger( EzLgoCommandDictionary.class.getName() );
    private final Map<String, Provider<? extends LgoCommand<?, ?>>> omapCommand = new HashMap<String, Provider<? extends LgoCommand<?, ?>>>();

    @Override
    public Collection<LgoCommand<?, ?>> guessCommand(String s_partial) {
        final LgoCommand<?, ?> command = buildCommand(s_partial);

        if (null == command) {
            return Collections.EMPTY_LIST;
        } else {
            List<LgoCommand<?, ?>> vSingle = new ArrayList<LgoCommand<?, ?>>();
            vSingle.add(command);
            return vSingle;
        }
    }

    @Override
    public LgoCommand<?, ?> buildCommand(String s_name) {
        final Provider<? extends LgoCommand<?,?>> provider = omapCommand.get(s_name);
        if ( null == provider ) {
            return null;
        } else {
            return provider.get();
        }
    }

    @Override
    public void setCommand(String s_name, Provider<? extends LgoCommand<?, ?>> provideCommand) {
        omapCommand.put(s_name, provideCommand);
    }

    @Override
    public Collection<Provider<? extends LgoCommand<?, ?>>> getCommands() {
        return omapCommand.values();
    }

    @Override
    public void setCommand(LgoHelp help, Provider<? extends LgoCommand<?, ?>> provideCommand) {
        setCommand(help.getFullName(), provideCommand);
        for (String sName : help.getShortNames()) {
            setCommand(sName, provideCommand);
        }
    }

    @Override
    public LgoHelp setCommand(LgoHelpLoader mgrHelp, Provider<? extends LgoCommand<?, ?>> provideCommand) {
        final LgoCommand<?,?> command = provideCommand.get();
        final LgoHelp help = mgrHelp.loadHelp( command.getName());
        if (null != help) {
            this.setCommand(help, provideCommand);
        } else {
            olog.log(Level.FINE, "No help available for command: " + command.getName());
            this.setCommand(command.getName(), provideCommand);
        }
        return help;
    }

    @Override
    public Provider<? extends LgoCommand<?, ?>> getProvider(String s_name) {
        return omapCommand.get( s_name );
    }
}
