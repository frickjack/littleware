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

import java.util.Collection;


/**
 * Just a little data-bucket for command-help
 */
public interface LgoHelp {
    /**
     * Getter for globally unique command name property.
     * Use normal reverse-DNS technique to
     * give each command a unique name.
     * 
     * @return the full-name string
     */
    public String getFullName();

    /**
     * Getter for short-name property.
     * Idea is that caller can usually use the short name(s)
     * unless two commands have the same short name.
     */
     public Collection<String> getShortNames();
     
    /**
     * Property getter gives brief synopsis of command use
     */
    public String getSynopsis ();
    
    /**
     * Property getter gives full description of command use
     */
    public String getDescription ();
    
    /**
     * Property getter for list of examples.
     */
    public Collection<LgoExample> getExamples();    
     
}
