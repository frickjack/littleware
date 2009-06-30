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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import littleware.apps.client.ClientBootstrap;
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.client.Feedback;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.security.auth.GuiceOSGiBootstrap;
import littleware.security.auth.LittleBootstrap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * Command-line based lgo launcher.
 * Lgo is just a stupid shell for launching
 * other commands as an alternative to
 * setting up .jar files or shell scripts
 * for launching each command.  Typical syntax is:
 *     lgo command-name command-args
 */
public class LgoCommandLine implements BundleActivator, Runnable {

    private static final Logger olog = Logger.getLogger(LgoCommandLine.class.getName());
    private static String[] ovArgs;
    private final LgoCommandDictionary omgrCommand;
    private final LgoHelpLoader omgrHelp;
    private final LittleBootstrap obootstrap;

    /**
     * Inject dependencies
     */
    @Inject
    public LgoCommandLine(
            LgoCommandDictionary mgrCommand,
            LgoHelpLoader mgrHelp,
            LittleBootstrap bootstrap,
            Provider<EzHelpCommand> comHelp,
            Provider<XmlEncodeCommand> comXml,
            Provider<LgoBrowserCommand> comBrowse,
            Provider<DeleteAssetCommand> comDelete,
            Provider<ListChildrenCommand> comLs,
            Provider<GetAssetCommand> comGet,
            Provider<CreateFolderCommand> comFolder,
            Provider<CreateUserCommand> comUser,
            Provider<CreateLockCommand> comLock,
            Provider<GetByNameCommand> comNameGet,
            Provider<SetImageCommand>  comSetImage
            ) {
        omgrCommand = mgrCommand;
        omgrHelp = mgrHelp;
        obootstrap = bootstrap;

        for (Provider<? extends LgoCommand<?,?>> command : // need to move this into a properties file
                Arrays.asList(
                    comHelp, comXml, comBrowse, comDelete, comLs, comGet,
                    comFolder, comUser, comLock, comNameGet, comSetImage
        )) {
            mgrCommand.setCommand(mgrHelp, (Provider<LgoCommand<?,?>>) command);
        }

    }
    private boolean obRunning = false;

    /** Launch worker thread once OSGi has started */
    @Override
    public void start(final BundleContext ctx) throws Exception {
        if (!obRunning) {
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ((evt.getType() == FrameworkEvent.STARTED) && (!obRunning)) {
                        obRunning = true;
                        ctx.removeFrameworkListener(this);
                        // launch onto dispatch thread
                        //SwingUtilities.invokeLater( LgoCommandLine.this );
                        new Thread(LgoCommandLine.this).start();
                    }
                }
            });
        }
    }

    /**
     * Issue the given command
     *
     * @param sCommand to lookup and run
     * @param vProcess argument to pass to processArgs
     * @param sArg to pass to command.runCommandLine
     * @param feedback to pass to command.runCommandLine
     * @return command exit-status
     */
    private int processCommand(String sCommand, List<String> vProcess, String sArg, Feedback feedback) {
        LgoCommand<?, ?> command = omgrCommand.buildCommand(sCommand);
        try {
            if (null == command) {
                System.out.print(omgrCommand.buildCommand("help").runCommandLine(feedback, ""));
                return 1;
            }

            command.processArgs(vProcess);

            String sResult = command.runCommandLine(feedback, sArg);
            System.out.println((null == sResult) ? "null" : sResult);
        } catch (LgoException ex) {
            System.out.println("Command failed, caught exception: " +
                    BaseException.getStackTrace(ex));
            try {
                System.out.print(omgrCommand.buildCommand("help").runCommand(feedback, command.getName()).toString());
            } catch (LgoException ex2) {
                throw new AssertionFailedException("Help command should not fail", ex2);
            }
            return 1;
        }
        return 0;
    }

    /**
     * Read command-lines from StdIn.
     * Command has form:
     *      name  args* [ -- arg]
     * Treat "exit" command as special escape.
     */
    private void processPipe(Feedback feedback) throws IOException {
        final BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionFailedException("What the frick?", ex);
        }

        final StringBuilder sb = new StringBuilder();
        System.out.println( "LGO>>" );
        for (String sLine = reader.readLine(); null != sLine; sLine = reader.readLine()) {
            String sClean = sLine.trim();
            if (sClean.length() == 0) {
                continue;
            }
            String sCommand = sClean;
            final List<String> vProcess = new ArrayList<String>();
            String sArg = "";
            final int iFirstSpace = sClean.indexOf(" ");

            if (iFirstSpace > 0) {
                sCommand = sClean.substring(0, iFirstSpace);
                sClean = sClean.substring(iFirstSpace);

                final int iDashDash = sClean.indexOf(" -- ");
                if (iDashDash >= 0) {
                    sArg = sClean.substring(iDashDash + 3).trim();
                    sClean = sClean.substring(0, iDashDash).trim();
                }
                // TODO - add some smarter parsing
                for( String sProcess : sClean.split("\\s+")) {
                    if ( sProcess.trim().length() > 0 ) {
                        vProcess.add( sProcess );
                    }
                }
            }
            if (sCommand.equalsIgnoreCase("exit")) {
                break;
            }
            processCommand(sCommand, vProcess, sArg, feedback);
            System.out.println( "LGO>>" );
        }
    }

    @Override
    public void run() {
        olog.log( Level.FINE, "Running on Swing dispatch thread" );
        String[] vArgs = getArgs();
        int iExitStatus = 0;

        try {
            Feedback feedback = new LoggerUiFeedback();

            if (vArgs.length == 0) { // launch help command by default
                System.out.print(omgrCommand.buildCommand("help").runCommandLine(feedback, ""));
                return;
            }
            final String sCommand = vArgs[0];
            if (sCommand.equalsIgnoreCase("pipe")) {
                processPipe(feedback);
                return;
            }
            List<String> vProcess = new ArrayList<String>();
            StringBuilder sb_in = new StringBuilder();

            for (int i = 1; i < vArgs.length; ++i) {
                String s_arg = vArgs[i];
                if (s_arg.equals("--")) {
                    // everything after -- goes into runCommand
                    for (int j = i + 1; j < vArgs.length; ++j) {
                        sb_in.append(vArgs[j]).append(" ");
                    // Note: trim() sb_in.toString before passing to run
                    }
                    break;
                }
                vProcess.add(s_arg);
            }

            iExitStatus = processCommand(sCommand, vProcess, sb_in.toString().trim(), feedback);
        } catch (Exception e) {
            iExitStatus = 1;
            olog.log(Level.SEVERE, "Failed command, caught: " + e, e);
        } finally {
            obootstrap.shutdown();
            System.exit(iExitStatus);
        }
    }

    /** NOOP */
    @Override
    public void stop(BundleContext ctx) throws Exception {
    }

    private static String[] getArgs() {
        return ovArgs;
    }

    /**
     * Look at the first argument to determine 
     * which command to launch, process arguments up until "--",
     * then pass pass the remaining args as a single space-separated string
     * to the command.runCommand method.
     * Should run on event-dispatch thread.
     * 
     * @param vArgs command-line args
     * @param bootClient to add LittleCommandLine.class to and bootstrap()
     */
    public static void launch(String[] vArgs, GuiceOSGiBootstrap bootClient) {
        ovArgs = vArgs;
        /*... just for testing in serverless environment ... 
        {
        // Try to start an internal server for now just for testing
        GuiceOSGiBootstrap bootServer = new littleware.security.auth.server.ServerBootstrap();
        bootServer.bootstrap();
        }
        ...*/

        bootClient.getOSGiActivator().add(LgoCommandLine.class);
        bootClient.bootstrap();
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
