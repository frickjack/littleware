/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Simple implementation of LgoCommandDictionary
 */
public class EzLgoCommandDictionary implements LgoCommandDictionary {
    private final Map<String,LgoCommand<?,?>>  omap_commands = new HashMap<String,LgoCommand<?,?>>();
    
    
    public Collection<LgoCommand<?,?>> guessCommand(String s_partial) {
        LgoCommand<?,?> command = getCommand( s_partial );
        
        if ( null == command ) {
            return Collections.EMPTY_LIST;
        } else {
            List<LgoCommand<?,?>> vSingle = new ArrayList<LgoCommand<?,?>> ();
            vSingle.add( command );
            return vSingle;
        }        
    }

    public LgoCommand<?,?> getCommand(String s_name) {
        return omap_commands.get( s_name );
    }

    public LgoCommand<?,?> setCommand(String s_name, LgoCommand<?,?> command) {
        return omap_commands.put( s_name, command);
    }
    
    public Collection<LgoCommand<?,?>> getCommands() {
        return omap_commands.values ();
    }
}
