/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

/**
 * Handy LgoCommand baseclass
 */
public abstract class AbstractLgoCommand implements LgoCommand {
    /**
     * Subtypes must initialize the name and help properties.
     * 
     * @param s_name
     * @param help
     */
    protected AbstractLgoCommand( String s_name ) {
        os_name = s_name;
    }
            
    private final String os_name;
    public String getName() {
        return os_name;
    }


    private String[] ov_args =new String[0];
    public String[] getCommandArgs() {
        return ov_args;
    }

    public void setCommandArgs(String[] v_args) {
        ov_args = v_args;
    }

    public abstract void runCommand() throws LgoException;

}
