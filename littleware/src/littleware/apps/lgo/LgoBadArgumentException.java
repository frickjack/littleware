/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;



/**
 * Base exception of littlego command tool.
 */
public class LgoBadArgumentException extends LgoException {

    public LgoBadArgumentException () {
        super();
    }
    
    public LgoBadArgumentException( String s_message ) {
        super( s_message );
    }
    
    public LgoBadArgumentException( String s_message, Throwable e_cause ) {
        super( s_message, e_cause );
    }
    
}
