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

import littleware.lgo.LgoHelpLoader;
import littleware.lgo.LgoCommandDictionary;
import littleware.lgo.LgoException;
import littleware.lgo.LgoCommand;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.security.auth.login.LoginException;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.EventBarrier;
import littleware.base.Maybe;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.LoggerFeedback;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.security.auth.client.ClientLoginModule;

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

    /** Inject dependencies */
    @Inject
    public LgoCommandLine(
            LgoCommandDictionary commandMgr,
            LgoHelpLoader helpMgr) {
        this.commandMgr = commandMgr;
        this.helpMgr = helpMgr;
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

            if (command instanceof LgoBrowserCommand) {
                // HACK!
                // Hard-code browser command exception for now ...
                final EventBarrier<Maybe<UUID>> barrier = ((LgoBrowserCommand) command).runCommand(feedback, sArg);
                System.out.println(barrier.waitForEventData().toString());
            } else {
                final String sResult = command.runCommandLine(feedback, sArg);
                System.out.println((null == sResult) ? "null" : sResult);
            }
        } catch (Exception ex) {
            System.out.println("Command failed, caught exception: "
                    + BaseException.getStackTrace(ex));
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
    public int run(String[] argsArray) {
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
        }
        return iExitStatus;
    }

    // ---------------------------------------
    /**
     * Look at the first argument to determine 
     * which command to launch, process arguments up until "--",
     * then pass pass the remaining args as a single space-separated string
     * to the command.runCommand method.
     * TODO - accept UI mode flag (swing, cli, swint-cli hybrid, server, ...)
     * 
     * @param argsIn command-line args
     * @param bootBuilder to add LittleCommandLine.class to and bootstrap()
     */
    public static void launch(final String[] argsIn, ClientBootstrap.ClientBuilder bootBuilder) {
        /*... just for testing in serverless environment ...
        {
            // Try to start an internal server for now just for testing
            final ServerBootstrap bootServer = littleware.bootstrap.server.ServerBootstrap.provider.get().build();
            bootServer.bootstrap();
        }
         */
        // bla
        final ClientLoginModule.ConfigurationBuilder loginBuilder = ClientLoginModule.newBuilder();
        String[] cleanArgs = argsIn;

        // Currently only support -url argument
        if ((argsIn.length > 1) && argsIn[0].matches("^-+[uU][rR][lL]")) {
            final String sUrl = argsIn[1];
            try {
                final URL url = new URL(sUrl);
                loginBuilder.host(url.getHost());
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Malformed URL: " + sUrl);
            }
            if (argsIn.length > 2) {
                cleanArgs = Arrays.copyOfRange(argsIn, 2, argsIn.length);
            } else {
                cleanArgs = new String[0];
            }
        }
        /*... need to rework this stuff ...
        if ( cleanArgs.length > 0 ) {
        final String command = cleanArgs[0];

        if (command.equalsIgnoreCase("pipe")) {
        bootBuilder.getOSGiActivator().add( LgoPipeActivator.class );
        bootBuilder.bootstrap();
        //processPipe(feedback);
        return;
        }

        if ( command.equalsIgnoreCase( "server" ) ) { // launch lgo server
        log.log( Level.INFO, "Launching lgo server ..." );
        bootBuilder.getOSGiActivator().add( LgoServerActivator.class );
        bootBuilder.bootstrap();
        return;
        }
        if ( command.equals( "jserver" ) ) { // launch lgo server - jnlp environment
        log.log( Level.INFO, "Launching lgo jnlp server ..." );
        bootBuilder.getOSGiActivator().add( JLgoServerActivator.class );
        bootBuilder.bootstrap();
        return;
        }
        }
         */
        int exitCode = 1;
        try {
            final ClientBootstrap boot = bootBuilder.build().automatic(loginBuilder.build());
            final LgoCommandLine cl = boot.bootstrap(LgoCommandLine.class);
            exitCode = cl.run(cleanArgs);
            boot.shutdown();
        } catch (LoginException ex) {
            log.log(Level.SEVERE, "Failed login", ex);
        }
        System.exit(exitCode);
    }

    /** Just launch( vArgs, new ClientSyncModule() ); on the event-dispatch thread */
    public static void main(final String[] vArgs) {
        /*..
        SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
        launch(vArgs, new ClientSyncModule());
        }
        });
         */
        launch(vArgs, ClientBootstrap.clientProvider.get());
    }
}
