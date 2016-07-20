package littleware.lgo;

import java.util.Locale;
import java.util.Optional;


/**
 * Loader loads help info out via class resource or whatever.
 * Maintains internal cache.
 * Just disassociates help-info from command implementation.
 */
public interface LgoHelpLoader {
    /**
     * Load the help-info for the given class and locale.
     * 
     * @param basename of help resource
     * @return the help info or null if no info available or
     *            if info cannot be parsed or whatever
     *            - parse error should be logged at a high level
     */
    public Optional<LgoHelp> loadHelp( String basename, Locale locale );

    /** Load help for the given class and the default Locale */
    public Optional<LgoHelp> loadHelp( String basename );
}
