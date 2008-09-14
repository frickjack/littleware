/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Interface for plugins to the LittleGo app-launcher.
 * Generic I is type of input to command, O is type of output.
 * 
 * TODO: refactor for asynchronous invocation - various
 * run methods should return a Handle for querying and killing
 * the child thread.
 */
public interface LgoCommand <InType,OutType> extends Cloneable {
    /**
     * Getter for globally unique command name property.
     * Use normal reverse-DNS technique to
     * give each command a unique name.
     * 
     * @return the full-name string
     */
    public String getName();

     
     
     /**
      * Set the command properties based
      * on the given command-line arguments
      * 
      * @param v_args to process
      * @exception LgoBadArgumentException if invalid args given
      */
     public void processCommandArgs( String[] v_args ) throws LgoException;
     
      
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
      * @exception IOException on problem accessing in/out streams
      */
      public void runCommand( InputStream istream,
              OutputStream ostream
              ) throws LgoException, IOException;
      
      /**
       * Run the command with the given input String,
       * and return the result string.
       * Equivalent to runCommand with StringReader/Writer
       * streams.
       * 
       * @param s_in to take as input
       * @return string representation of result
       */
      public String runCommand( String s_in 
              ) throws LgoException, IOException;
       

      /**
       * Run in case where caller is not 
       * keeping track of the type of the input object.
       * 
       * @param x_in cast internally to InType
       * @return result of command
       * @throws littleware.apps.lgo.LgoException
       * @throws ClassCastException on failure to cast x_in to a supported type
       */
      public OutType runDynamic ( Object x_in ) throws LgoException;
      
      /** Run type-safe */
      public OutType runSafe( InType in ) throws LgoException;
      
      /** 
       * Specialization of clone - no exception, and return LgoCommand
       * instance.
       */
      public LgoCommand clone ();
}
