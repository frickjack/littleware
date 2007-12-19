/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.util.List;


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
     * Idea is that caller can usually use the short name
     * unless two commands have the same short name.
     */
     public String getShortName();
     
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
    public List<LgoExample> getExamples();
    
    /**
     * Convenience function - allows retrieval of example by name
     */
    public LgoExample getExample( String s_name );
     
}
