/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import littleware.apps.client.UiFeedback;


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
     * give each command subtype a unique name.
     * 
     * @return the full-name string
     */
    public String getName();

     
     /**
      * Set the command properties based
      * on the given command-line arguments
      * 
      * @param v_args to process - series of -flag1, -flag2, value, etc.
      * @exception LgoBadArgumentException if invalid args given
      */
     public void processArgs( List<String> v_args ) throws LgoException;
     
      
     /**
      * Run the command with the given input 
      * and output streams.
      * Mostly pass input/output to simplify testing,
      * but also allows pipes in the future.
      *
      * @param feedback for UI feedback on progress, etc. - not a data channel
      * @param istream  to pull input from
      * @param ostream to send output to
      * @exception LgoBadArgumentException if arguments are invalid
      * @exception LgoBadInputException if input cannot be handled
      * @exception IOException on problem accessing in/out streams
      */
      public void runCommand( UiFeedback feedback, InputStream istream,
              OutputStream ostream
              ) throws LgoException, IOException;

      /**
       * Facilitate simple command-line interaction - string and args in,
       * formatted string out
       *
       * @param feedback
       * @param s_in
       * @return formatted string result
       * @throws littleware.apps.lgo.LgoException
       */
      public String runCommandLine( UiFeedback feedback, String s_in ) throws LgoException;
      
      /**
       * Run the command with the given input String,
       * and return the result string.
       * Equivalent to runCommand with StringReader/Writer
       * in/out streams.
       *
       * @param feedback for UI feedback on progress, etc. - not a data channel
       * @param s_in to take as input and convert to InType
       * @return string representation of result
       */
      public OutType runCommand(
              UiFeedback feedback, String s_in
              ) throws LgoException;
       

      /**
       * Run in case where caller is not 
       * keeping track of the type of the input object.
       *
       * @param feedback for UI feedback on progress, etc. - not a data channel
       * @param x_in cast internally to InType
       * @return result of command
       * @throws littleware.apps.lgo.LgoException
       * @throws ClassCastException on failure to cast x_in to a supported type
       */
      public OutType runDynamic ( UiFeedback feedback,
              Object x_in ) throws LgoException;
      
      /** Run type-safe */
      public OutType runSafe( UiFeedback feedback, InType in ) throws LgoException;
      
      /** 
       * Specialization of clone - no exception, and return LgoCommand
       * instance.
       */
      public LgoCommand clone ();
}
