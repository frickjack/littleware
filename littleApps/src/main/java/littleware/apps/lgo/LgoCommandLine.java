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

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import littleware.apps.client.ClientBootstrap;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.LoggerFeedback;
import littleware.security.auth.LittleBootstrap;

import littleware.security.auth.RunnerActivator;
import org.osgi.framework.BundleContext;

/**
 * Command-line based lgo launcher.
 * Lgo is just a stupid shell for launching
 * other commands as an alternative to
 * setting up .jar files or shell scripts
 * for launching each command.  Typical syntax is:
 *     lgo command-name command-args
 */
public class LgoCommandLine {

    private static final Logger log = Logger.getLogger(LgoCommandLine.class.getName());

    private final LgoCommandDictionary commandMgr;
    private final LgoHelpLoader helpMgr;
    private final LittleBootstrap bootstrap;
    

    /** Inject dependencies */
    @Inject
    public LgoCommandLine(
            LgoCommandDictionary commandMgr,
            LgoHelpLoader helpMgr,
            LittleBootstrap bootstrap
            ) {
        this.commandMgr = commandMgr;
        this.helpMgr = helpMgr;
        this.bootstrap = bootstrap;
    }

    /**
     * Issue the given command
     *
     * @param sCommand to lookup and run
     * @param processArgs argument to pass to processArgs
     * @param sArg to pass to command.runCommandLine
     * @param feedback to pass to command.runCommandLine
     * @return command exit-status
     */
    public int processCommand(String sCommand, List<String> processArgs, String sArg, Feedback feedback) {
        final LgoCommand<?, ?> command = commandMgr.buildCommand(sCommand);
        try {
            if (null == command) {
                System.out.print(commandMgr.buildCommand("help").runCommandLine(feedback, ""));
                return 1;
            }

            command.processArgs(processArgs);

            String sResult = command.runCommandLine(feedback, sArg);
            System.out.println((null == sResult) ? "null" : sResult);
        } catch (LgoException ex) {
            System.out.println("Command failed, caught exception: " +
                    BaseException.getStackTrace(ex));
            try {
                System.out.print(commandMgr.buildCommand("help").runCommand(feedback, command.getName()).toString());
            } catch (LgoException ex2) {
                throw new AssertionFailedException("Help command should not fail", ex2);
            }
            return 1;
        }
        return 0;
    }


    /**
     * Run the LgoCommand specifiedby the given args array
     *
     * @param argsArray lgo command line arguments
     * @return command exit code
     */
    public int run( String[] argsArray ) {
        log.log(Level.FINE, "Running on Swing dispatch thread");
        int iExitStatus = 0;

        try {
            final Feedback feedback = new LoggerFeedback();

            if (argsArray.length == 0) { // launch help command by default
                System.out.print(commandMgr.buildCommand("help").runCommandLine(feedback, ""));
                return 1;
            }
            final String command = argsArray[0];
            final List<String> cleanArgs = new ArrayList<String>();
            final StringBuilder sb = new StringBuilder();

            for (int i = 1; i < argsArray.length; ++i) {
                final String arg = argsArray[i];
                if (arg.equals("--")) {
                    // everything after -- goes into runCommand
                    for (int j = i + 1; j < argsArray.length; ++j) {
                        sb.append(argsArray[j]).append(" ");
                        // Note: trim() sb_in.toString before passing to run
                    }
                    break;
                }
                cleanArgs.add(arg);
            }
            iExitStatus = processCommand(command, cleanArgs, sb.toString().trim(), feedback);
        } catch (Exception e) {
            iExitStatus = 1;
            log.log(Level.SEVERE, "Failed command, caught: " + e, e);
        } finally {
            bootstrap.shutdown();
        }
        return iExitStatus;
    }



    /**
     * Look at the first argument to determine 
     * which command to launch, process arguments up until "--",
     * then pass pass the remaining args as a single space-separated string
     * to the command.runCommand method.
     * Should run on event-dispatch thread.
     * 
     * @param argsIn command-line args
     * @param bootClient to add LittleCommandLine.class to and bootstrap()
     */
    public static void launch( final String[] argsIn, ClientBootstrap bootClient) {
        /*... just for testing in serverless environment ... 
        {
        // Try to start an internal server for now just for testing
        GuiceOSGiBootstrap bootServer = new littleware.security.auth.server.ServerBootstrap();
        bootServer.bootstrap();
        }
        ...*/
        String[] cleanArgs = argsIn;

        // Currently only support -url argument
        if ((argsIn.length > 1) && argsIn[0].matches("^-+[uU][rR][lL]")) {
            final String sUrl = argsIn[1];
            try {
                final URL url = new URL(sUrl);
                bootClient.setHost(Maybe.something(url.getHost()));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Malformed URL: " + sUrl);
            }
            if (argsIn.length > 2) {
                cleanArgs = Arrays.copyOfRange(argsIn, 2, argsIn.length );
            } else {
                cleanArgs = new String[0];
            }
        }
        if ( cleanArgs.length > 0 ) {
            final String command = cleanArgs[0];
            
            if (command.equalsIgnoreCase("pipe")) {
                bootClient.getOSGiActivator().add( LgoPipeActivator.class );
                bootClient.bootstrap();
                //processPipe(feedback);
                return;
            }
            if ( command.equalsIgnoreCase( "server" ) ) { // launch lgo server
                log.log( Level.INFO, "Launching lgo server ..." );
                bootClient.getOSGiActivator().add( LgoServerActivator.class );
                bootClient.bootstrap();
                return;
            }
            if ( command.equals( "jserver" ) ) { // launch lgo server - jnlp environment
                log.log( Level.INFO, "Launching lgo jnlp server ..." );
                bootClient.getOSGiActivator().add( JLgoServerActivator.class );
                bootClient.bootstrap();
                return;
            }
        } 
        final LgoCommandLine cl = bootClient.bootstrap( LgoCommandLine.class );
        cl.run( cleanArgs );
    }


    /** Just launch( vArgs, new ClientBootstrap() ); on the event-dispatch thread */
    public static void main(final String[] vArgs) {
        /*..
        SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
        launch(vArgs, new ClientBootstrap());
        }
        });
         */
        launch(vArgs, new ClientBootstrap());
    }
}
