/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.io.InputStream;


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
      * Get the id of this command within the pipe
      */
     public int getId ();
     
     /**
      * Getter for help-info property.
      */
     public LgoHelp getHelpInfo();
     
     /**
      * Property for command-parameters configuring
      * the way the command should behave.
      */
     public String[] getCommandArgs();
     public void setCommandArgs( String[] v_args );
     

     /**
      * Run the command with the given input stream      
      */
      public void runCommand( InputStream input  )
              throws LgoException;
      
     /**
      * Run the command with the given input stream      
      * 
      * @param input stream to pull input from
      * @exception LgoBadArgumentException if arguments are invalid
      * @exception LgoBadInputException if input cannot be handled
      */
      public void runCommand( InputStream input  )
              throws LgoException;

}
