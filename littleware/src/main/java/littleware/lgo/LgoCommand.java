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

import java.util.List;
import littleware.base.feedback.Feedback;

/**
 * Interface for command objects that can be 
 * executed via the LittleGo app-launcher or servlet.
 */
public interface LgoCommand {

    /**
     * Getter for globally unique command name property.
     * Use normal reverse-DNS technique to
     * give each command subtype a unique name.
     * 
     * @return the full-name string
     */
    public String getName();

    public String runCommandLine( Feedback feedback ) throws Exception;
    public Object runCommand( Feedback feedback ) throws Exception;

    public interface LgoBuilder {
        public String getName();
        public LgoCommand buildFromArgs(List<String> args);
        public LgoCommand buildWithInput( Object input );
    }

}
