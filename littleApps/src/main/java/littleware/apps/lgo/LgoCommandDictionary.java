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

import com.google.inject.Provider;
import java.util.Collection;

/**
 * Interface for command parsers.
 */
public interface LgoCommandDictionary {
    
    /**
     * Guess 1+ commands that the given partial
     * name might match or be intended to match.
     * 
     * @param s_partial
     * @return zero or more possible commands
     */
    public Collection<LgoCommand<?,?>> guessCommand( String s_partial );
    
    /**
     * Build a command by name (full or short)
     * 
     * @return null if no match or single match     
     */
    public LgoCommand<?,?> buildCommand( String s_name );
    
    /**
     * Get the provider of a command if any
     * 
     * @param s_name
     * @return provider if registered or null if none
     */
    public Provider<? extends LgoCommand<?,?>> getProvider( String s_name );
     
    /**
     * Associate the given provider with the given command-name
     * 
     * @param s_name alias to map command to
     * @param command to map to s_name alias
     */
    public void setCommand( String s_name, Provider<? extends LgoCommand<?,?>> provideCommand );

    /**
     * Associate the given provider with the given command-name and
     * all the command aliases provided by the help info
     *
     * @param help info with full name and short names to associate with the command
     * @param command to map to info aliases
     */
    public void setCommand( LgoHelp help, Provider<? extends LgoCommand<?,?>> provideCommand );

    /**
     * Another utility - loads the help associated with command,
     * and call setCommand(help,command) if help is non-null,
     * otherwise just setCommand(command.getName(),command).
     * 
     * @param mgrHelp to load help info with
     * @param command to register
     * @return loaded help - may be null
     */
    public LgoHelp setCommand(LgoHelpLoader mgrHelp, Provider<? extends LgoCommand<?,?>> provideCommand );

    /**
     * Get the collection of all the commands registered with
     * the dictionary.
     * 
     * @return collection of commands
     */
    public Collection<Provider<? extends LgoCommand<?,?>>> getCommands();
}
