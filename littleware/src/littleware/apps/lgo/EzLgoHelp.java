/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.util.Collection;


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

}
