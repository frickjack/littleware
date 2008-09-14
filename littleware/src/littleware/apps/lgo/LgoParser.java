/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

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
