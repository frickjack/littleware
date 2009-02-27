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
    private final Map<String, LgoCommand<?, ?>> omap_commands = new HashMap<String, LgoCommand<?, ?>>();

    public Collection<LgoCommand<?, ?>> guessCommand(String s_partial) {
        LgoCommand<?, ?> command = getCommand(s_partial);

        if (null == command) {
            return Collections.EMPTY_LIST;
        } else {
            List<LgoCommand<?, ?>> vSingle = new ArrayList<LgoCommand<?, ?>>();
            vSingle.add(command);
            return vSingle;
        }
    }

    public LgoCommand<?, ?> getCommand(String s_name) {
        return omap_commands.get(s_name);
    }

    public LgoCommand<?, ?> setCommand(String s_name, LgoCommand<?, ?> command) {
        return omap_commands.put(s_name, command);
    }

    public Collection<LgoCommand<?, ?>> getCommands() {
        return omap_commands.values();
    }

    public void setCommand(LgoHelp help, LgoCommand<?, ?> command) {
        setCommand(help.getFullName(), command);
        for (String sName : help.getShortNames()) {
            setCommand(sName, command);
        }
    }

    public LgoHelp setCommand(LgoHelpLoader mgrHelp, LgoCommand<?, ?> command) {
        LgoHelp help = mgrHelp.loadHelp(command.getName());
        if (null != help) {
            this.setCommand(help, command);
        } else {
            olog.log(Level.FINE, "No help available for command: " + command.getName());
            this.setCommand(command.getName(), command);
        }
        return help;
    }
}
