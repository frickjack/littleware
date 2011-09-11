/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo;

import java.util.Locale;
import littleware.base.Option;


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
    public Option<LgoHelp> loadHelp( String basename, Locale locale );

    /** Load help for the given class and the default Locale */
    public Option<LgoHelp> loadHelp( String basename );
}
