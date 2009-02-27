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
import java.util.ArrayList;
import java.util.List;
import littleware.apps.client.ClientBootstrap;
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.client.UiFeedback;
import littleware.security.auth.GuiceOSGiBootstrap;
import littleware.security.auth.LittleBootstrap;
import littleware.security.auth.server.ServerBootstrap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
    private final LgoHelpLoader        omgrHelp;
    private final LittleBootstrap      obootstrap;

    /**
     * Inject dependencies
     */
    @Inject
    public LgoCommandLine(
            LgoCommandDictionary mgrCommand,
            LgoHelpLoader mgrHelp,
            LittleBootstrap bootstrap,
            EzHelpCommand comHelp,
            XmlEncodeCommand comXml,
            LgoBrowserCommand comBrowse) {
        omgrCommand = mgrCommand;
        omgrHelp = mgrHelp;
        obootstrap = bootstrap;

        for (LgoCommand command : // need to move this into a properties file
                new LgoCommand[]{
                    comHelp, comXml, comBrowse
                }) {
            mgrCommand.setCommand( mgrHelp, command );
        }

    }


    /** Launch worker thread */
    public void start(BundleContext ctx) throws Exception {
        new Thread( this ).start();
    }

    public void run() {
        String[] vArgs = getArgs();

        try {
            /*..
            Injector     injector = Guice.createInjector(
            new EzModule(),
            new littleware.apps.swingclient.StandardSwingGuice(),
            new littleware.apps.client.StandardClientGuice(),
            new littleware.apps.misc.StandardMiscGuice(),
            new littleware.security.auth.ClientServiceGuice(),
            new PropertiesGuice( littleware.base.PropertiesLoader.get().loadProperties() )
            );
            LgoCommandDictionary m_command = injector.getInstance( LgoCommandDictionary.class );
            LgoHelpLoader        m_help = injector.getInstance( LgoHelpLoader.class );
             */
            UiFeedback feedback = new LoggerUiFeedback();

            if (vArgs.length == 0) { // launch help command by default
                System.out.print(omgrCommand.getCommand("help").runCommand(feedback, "help").toString());
                return;
            }
            LgoCommand<?, ?> command = omgrCommand.getCommand(vArgs[0]); // injector.getInstance( LgoBrowserCommand.class ); //m_command.getCommand( LgoBrowserCommand.class.getName () );
            if (null == command) {
                System.out.print(omgrCommand.getCommand("help").runCommand(feedback, "help").toString());
                return;
            }
            List<String> v_process = new ArrayList<String>();
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
                v_process.add(s_arg);
            }
            command.processArgs(v_process);
            Object result = command.runCommand(feedback, sb_in.toString().trim());
            System.out.println( (null == result) ? "null" : result.toString() );
        } catch (Exception e) {
            olog.log(Level.SEVERE, "Failed command, caught: " + e, e);
        } finally {
            obootstrap.shutdown();
        }
    }

    public void stop(BundleContext arg0) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static String[] getArgs() { return ovArgs; }

    /**
     * Look at the first argument to determine 
     * which command to launch, process arguments up until "--",
     * then pass pass the remaining args as a single space-separated string
     * to the command.runCommand method.
     * 
     * @param vArgs command-line args
     * @param bootClient to add LittleCommandLine.class to and bootstrap()
     */
    public static void launch(String[] vArgs, GuiceOSGiBootstrap bootClient ) {
        ovArgs = vArgs;
        {
            // Try to start an internal server for now just for testing
            GuiceOSGiBootstrap bootServer = new ServerBootstrap();
            bootServer.bootstrap();
        }

        bootClient.getOSGiActivator().add( LgoCommandLine.class );
        bootClient.bootstrap();
    }

    /** Just launch( vArgs, new ClientBootstrap() ); */
    public static void main( String[] vArgs ) {
        launch( vArgs, new ClientBootstrap() );
    }
    
}
