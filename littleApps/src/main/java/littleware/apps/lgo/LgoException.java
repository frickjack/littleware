/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import littleware.base.BaseException;


/**
 * Base exception of littlego command tool.
 */
public class LgoException extends BaseException {

    public LgoException () {
        super();
    }
    
    public LgoException( String s_message ) {
        super( s_message );
    }
    
    public LgoException( String s_message, Throwable e_cause ) {
        super( s_message, e_cause );
    }
    
}
