/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

/**
 * Just an example of how to use a command -
 * delegate of LgoHelp.
 */
public interface LgoExample {
    /**
     * Property give the example a name - should
     * be unique within the LgoHelp instance.
     */
    public String getName ();
    
    /**
     * Title property
     */
    public String getTitle ();
    
    /**
     * The example.
     */
    public String getDescription ();
}
