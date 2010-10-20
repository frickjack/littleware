/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo;

import littleware.lgo.LgoCommand;
import java.util.Collection;
import java.util.List;

/**
 * Interface for command parsers.
 */
public interface LgoParser {
    
    /**
     * Guess 1+ commands that the given partial
     * name might match or be intended to match.
     * 
     * @param s_partial
     * @return zero or more possible commands
     */
    public Collection<LgoCommand> guessCommand( String s_partial );
    
    /**
     * Get a command by name (full or short)
     * 
     * @return null if no match or single match     
     */
    public LgoCommand getCommand( String s_name );
     
    /**
     * Associate the given provider with the given command-name
     */
    public LgoCommand setCommand( String s_name, LgoCommand command );     
}
