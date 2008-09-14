/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.util.Locale;


/**
 * Loader loads help info out via class resource or whatever.
 * Maintains internal cache.
 * Just disassociates help-info from command implementation.
 */
public interface LgoHelpLoader {
    /**
     * Load the help-info for the given class and locale.
     * 
     * @param s_basename of help resource
     * @return the help info or null if no info available or
     *            if info cannot be parsed or whatever
     *            - parse error should be logged at a high level
     */
    public LgoHelp loadHelp( String s_basename, Locale locale );

    /** Load help for the given class and the default Locale */
    public LgoHelp loadHelp( String s_basename );
}
