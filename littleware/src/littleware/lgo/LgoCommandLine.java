/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.lgo;

import com.google.common.collect.ImmutableMap;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.LoginException;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.LoggerFeedback;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.client.AppBootstrap;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.security.auth.client.ClientLoginModule;

/**
 * Command-line based lgo launcher.
 * Lgo is just a stupid shell for launching
 * other commands as an alternative to
 * setting up .jar files or shell scripts
 * for launching each command.  Typical syntax is:
 *     lgo [special options] command-name command-args
 */
public class LgoCommandLine {

    private static final Logger log = Logger.getLogger(LgoCommandLine.class.getName());

    /**
     * Special options that LgoCommandLine.launch looks for as first
     * arguments before the command's name:
     *  +mode [local|client] -- client establishes session with littleware server, default is client
     *  +profile [cli|swing] -- specify AppProfile to pass to AppModuleFactory modules at bootup time
     *  +user [username] -- user to authenticate as in client mode, defaults to current OS user
     *  +url  [server-info] -- url specifies host/port information for the littleware server in client mode
     */
    private enum SpecialOption {
        user, url, mode, profile;
    };

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
        final Maybe<LgoCommand.LgoBuilder> maybe = commandMgr.buildCommand(sCommand);
        try {
            if (maybe.isEmpty() || sCommand.equalsIgnoreCase("help")) {
                System.out.println("Command use:");
                System.out.println("     lgo [+user name] [+mode client|app] [+url http://server:port] [+profile cli|swing] command options");
                System.out.println("        +mode [local|client] -- client establishes session with littleware server, default is client");
                System.out.println("        +profile [cli|swing] -- specify AppProfile to pass to AppModuleFactory modules at bootup time");
                System.out.println("        +user [username] -- user to authenticate as in client mode, defaults to current OS user");
                System.out.println("        +url  [server-info] -- url specifies host/port information for the littleware server in client mode\n");
            }
            if (maybe.isEmpty()) {
                final LgoCommand help = commandMgr.buildCommand("help").get().buildWithInput(sCommand);
                System.out.print(help.runCommandLine(feedback));
                return 1;
            }

            final String result = maybe.get().buildFromArgs(processArgs).runCommandLine(feedback);
            System.out.println((null == result) ? "null" : result);
        } catch (Exception ex) {
            System.out.println("Command failed: "
                    + BaseException.getStackTrace(ex));

            try {
                System.out.print(commandMgr.buildCommand("help").get().buildWithInput(sCommand).runCommandLine(feedback));
            } catch (Exception ex2) {
                throw new AssertionFailedException("Help command should not fail", ex2);
            }
            return 1;
        }
        return 0;
    }

    /**
     * Run the LgoCommand specified by the given args array
     *
     * @param argsArray lgo command line arguments
     * @return command exit code
     */
    public int run(String[] argsArray) {
        int iExitStatus = 0;

        try {
            final Feedback feedback = new LoggerFeedback();

            if (argsArray.length == 0) { // launch help command by default
                System.out.print(commandMgr.buildCommand("help").get().buildWithInput("").runCommandLine(feedback));
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
            log.log(Level.SEVERE, "Failed command", e);
        }
        return iExitStatus;
    }

    private static String arrayToString(String[] in) {
        final StringBuilder sb = (new StringBuilder()).append("[");
        for (String scan : in) {
            sb.append(" ").append(scan).append(",");
        }
        return sb.append(" ]").toString();
    }

    // ---------------------------------------
    /**
     * Launch configures its own bootstrap launcher
     * based on a set of special options that.
     * The first non-special argument determines
     * which command to launch, and the arguments after that
     * are passed to the command-builder's process-argument option.
     * Shuts down the littleware bootstrap runtime after the command returns.
     *
     * @param argsIn command-line args
     */
    public static void launch(final String[] argsIn) {
        /*... just for testing in serverless environment ...
        {
        // Try to start an internal server for now just for testing
        final ServerBootstrap bootServer = littleware.bootstrap.server.ServerBootstrap.provider.get().build();
        bootServer.bootstrap();
        }
         */

        // Support +url, +user, ...
        final String[] cleanArgs;
        log.log(Level.FINE, "Processing args: " + arrayToString(argsIn));
        final Map<SpecialOption, String> specialOptionMap;
        {
            final ImmutableMap.Builder<SpecialOption, String> builder = ImmutableMap.builder();
            String[] tempArgs = argsIn;
            while (tempArgs.length > 1) {
                boolean isSpecial = false;
                for (SpecialOption option : SpecialOption.values()) {
                    if (tempArgs[0].toLowerCase().matches("^[-\\+]+" + option.toString())) {
                        builder.put(option, tempArgs[1]);

                        if (tempArgs.length > 2) {
                            tempArgs = Arrays.copyOfRange(tempArgs, 2, tempArgs.length);
                        } else {
                            tempArgs = new String[0];
                        }
                        isSpecial = true;
                        break;
                    }
                }
                if (!isSpecial) {
                    break;
                }
            }
            specialOptionMap = builder.build();
            cleanArgs = tempArgs;
        }
        log.log(Level.FINE, "Clean args: " + arrayToString(cleanArgs));
        final AppBootstrap.AppProfile profile;
        if (specialOptionMap.containsKey(SpecialOption.profile)) {
            final String profileName = specialOptionMap.get(SpecialOption.profile).toLowerCase().trim();
            if (profileName.equals("cli")) {
                profile = AppBootstrap.AppProfile.CliApp;
            } else {
                profile = AppBootstrap.AppProfile.SwingApp;
            }
        } else {
            profile = AppBootstrap.AppProfile.SwingApp;
        }

        Maybe<LittleBootstrap> maybeBoot = Maybe.empty();
        if ((!specialOptionMap.containsKey(SpecialOption.mode))
                || specialOptionMap.get(SpecialOption.mode).toLowerCase().trim().equals("client")) {
            final ClientLoginModule.ConfigurationBuilder loginBuilder = ClientLoginModule.newBuilder();
            if (specialOptionMap.containsKey(SpecialOption.url)) {
                final String sUrl = specialOptionMap.get(SpecialOption.url);
                try {
                    final URL url = new URL(sUrl);
                    loginBuilder.host(url.getHost());
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException("Malformed URL: " + sUrl);
                }
            }
            final Maybe<String> maybeUser;
            if ( specialOptionMap.containsKey(SpecialOption.user) ) {
                maybeUser = Maybe.something( specialOptionMap.get( SpecialOption.user ));
            } else {
                maybeUser = Maybe.empty();
            }

            /*... need to rework this pipe/server stuff ...
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

            try {
                final ClientBootstrap.ClientBuilder bootBuilder = ClientBootstrap.clientProvider.get().profile(profile);
                if (maybeUser.isSet()) {
                    maybeBoot = Maybe.something((LittleBootstrap) bootBuilder.build().automatic(
                            loginBuilder.build(), maybeUser.get(), "")
                            );
                } else {
                    maybeBoot = Maybe.something((LittleBootstrap) bootBuilder.build().automatic(loginBuilder.build()));
                }

            } catch (LoginException ex) {
                log.log(Level.SEVERE, "Failed login", ex);
            }
        } else {
            maybeBoot = Maybe.something((LittleBootstrap) AppBootstrap.appProvider.get().profile(profile).build());
        }
        int exitCode = 1;
        if (maybeBoot.isSet()) {
            final LittleBootstrap boot = maybeBoot.get();
            final LgoCommandLine cl = boot.bootstrap(LgoCommandLine.class);
            try {
                exitCode = cl.run(cleanArgs);
            } finally {
                boot.shutdown();
            }
        }
        System.exit(exitCode);
    }

    /** Just launch( vArgs, new ClientSyncModule() ); on the event-dispatch thread */
    public static void main(final String[] vArgs) {
        launch(vArgs);
    }
}
