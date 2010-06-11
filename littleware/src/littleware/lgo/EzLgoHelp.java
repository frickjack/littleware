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

import littleware.lgo.LgoHelp;
import littleware.lgo.LgoExample;
import java.util.Collection;
import littleware.base.Whatever;


/**
 * Data bucket LgoHelp implementation
 */
public class EzLgoHelp implements LgoHelp {

    /**
     * Constructor intiializes all the read-only properties
     * with references to the given parameters.
     */
    public EzLgoHelp( String s_fullname,
            Collection<String> v_short_names,
            String s_synopsis,
            String s_description,
            Collection<LgoExample> v_examples
            ) 
    {
        os_fullname = s_fullname;
        ov_short_names = v_short_names;
        os_synopsis = s_synopsis;
        os_description = s_description;
        ov_examples = v_examples;
    }
    
    private final String os_description;
    public String getDescription() {
        return os_description;
    }

    private final Collection<LgoExample>  ov_examples;
    

    public Collection<LgoExample> getExamples() {
        return ov_examples;
    }

    private final String  os_fullname;
    public String getFullName() {
        return os_fullname;
    }

    private Collection<String> ov_short_names;
    public Collection<String> getShortNames() {
        return ov_short_names;
    }

    private final String  os_synopsis;
    public String getSynopsis() {
        return os_synopsis;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ();
        sb.append( getFullName () ).append( ":" ).
                append( Whatever.NEWLINE ).append( "   " ).
                append( getSynopsis() ).append( Whatever.NEWLINE ).
                append( Whatever.NEWLINE ).
                append( getDescription() ).
                append( Whatever.NEWLINE );
        return sb.toString();
    }
}
