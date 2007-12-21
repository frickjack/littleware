/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * Interface for plugins to the LittleGo app-launcher.
 * Generic I is type of input to command, O is type of output.
 */
public interface LgoCommand {
    /**
     * Getter for globally unique command name property.
     * Use normal reverse-DNS technique to
     * give each command a unique name.
     * 
     * @return the full-name string
     */
    public String getName();

     
     
     /**
      * Property for command-parameters configuring
      * the way the command should behave.
      */
     public String[] getCommandArgs();
     public void setCommandArgs( String[] v_args );
     
      
     /**
      * Run the command with the given input 
      * and output streams.
      * Mostly pass input/output to simplify testing,
      * but also allows pipes in the future.
      * 
      * @param istream  to pull input from
      * @param ostream to send output to
      * @exception LgoBadArgumentException if arguments are invalid
      * @exception LgoBadInputException if input cannot be handled
      */
      public void runCommand() throws LgoException;

}
