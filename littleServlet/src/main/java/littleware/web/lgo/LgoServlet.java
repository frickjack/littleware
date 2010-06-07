/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetSearchManager;
import littleware.base.EventBarrier;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.NullFeedback;
import littleware.security.auth.SessionHelper;
import littleware.web.servlet.LittleServlet;

/**
 * Lgo command servlet for embedded server
 */
public class LgoServlet extends LittleServlet {
    private static final Logger log = Logger.getLogger( LgoServlet.class.getName() );

    private LgoCommandDictionary commandMgr;
    private LgoHelpLoader helpMgr;
    private LittleBootstrap bootstrap;
    private Provider<Gson> jsonProvider;
    private AssetSearchManager search;
    private SessionHelper helper;
    private String serverVersion;
    private Date  lastVersionCheck = new Date();

    @Inject
    public void injectMe(
            LgoCommandDictionary commandMgr,
            LgoHelpLoader helpMgr,
            LittleBootstrap bootstrap,
            Provider<Gson> jsonProvider,
            AssetSearchManager search,
            SessionHelper helper,
            @Named("littleware.startupServerVersion") String serverVersion
            ) {
        this.commandMgr = commandMgr;
        this.helpMgr = helpMgr;
        this.bootstrap = bootstrap;
        this.jsonProvider = jsonProvider;
        this.search = search;
        this.helper = helper;
        this.serverVersion = serverVersion;
    }
    private final Feedback feedback = new NullFeedback();

    /**
     * Issue the given command
     *
     * @param command to lookup and run
     * @param processArgs argument to pass to processArgs
     * @param sArg to pass to command.runCommandLine
     * @param feedback to pass to command.runCommandLine
     * @return command output
     */
    private String processCommand(String commandName, List<String> processArgs, String sArg) throws LgoException {
        final LgoCommand<?, ?> command = commandMgr.buildCommand(commandName);
        if (null == command) {
            throw new LgoArgException("No command found: " + commandName);
        }
        command.processArgs(processArgs);

        final Gson gson = jsonProvider.get();
        final Object output = command.runDynamic(feedback, sArg);
        final String result;
        if ( output instanceof  Asset ) {
            result = gson.toJson( output, Asset.class );
        } else if ( output instanceof AssetPath ) {
            result = gson.toJson( output, AssetPath.class );
        } else if ( output instanceof LgoHelp ) {
            result = output.toString();
        } else if ( output instanceof EventBarrier ) {
            result = gson.toJson( "EventBarrier" );
        } else {
            result = gson.toJson( output );
        }
        return (null == result) ? "" : result;
    }


    @Override
    public void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String input = "";
        final List<String> argList = new ArrayList<String>();
        for (Enumeration keyEnum = request.getParameterNames();
                keyEnum.hasMoreElements();) {
            final String key = (String) keyEnum.nextElement();
            if (key.equals("input")) {
                input = request.getParameterValues(key)[0];
            } else {
                argList.add("-" + key);
                argList.addAll(Arrays.asList(request.getParameterValues(key)));
            }
        }
        final String command;
        {
            final String temp = request.getPathInfo();
            final int lastSlash = temp.lastIndexOf('/');
            if (lastSlash >= 0) {
                command = temp.substring(lastSlash + 1);
            } else {
                command = temp;
            }
        }
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<html><head><title>" + command + "</title><body><pre id=\"data\"><data>");

        String result;
        try {
            if ( ! command.equals( "exit" ) ) {
                result = processCommand(command, argList, input);
            } else {
                result = "Shutting down ...";
                bootstrap.shutdown();
            }
        } catch (Exception ex) {
            result = "Exception!: " + littleware.base.BaseException.getStackTrace(ex);
        }
        response.getWriter().println( result.replaceAll( "<", "&lt;").replaceAll( ">", "&gt;") );
        response.getWriter().println("</data></pre></body></html>");
        // Finally, check if the server has booted up a new version on us
        checkServerVersion();
    }

    private void checkServerVersion() {
        boolean shutdown = false;
        try {
            final Date now = new Date();
            if ( now.getTime() > lastVersionCheck.getTime() + 120000 ) {
                // check every 2 minutes
                lastVersionCheck = now;
                final String currentVersion = helper.getServerVersion();
                if ( ! serverVersion.equals( currentVersion ) ) {
                    log.log( Level.WARNING, "Shutting down after server version check: " + serverVersion + " != " + currentVersion );
                    shutdown = true;
                }
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed server version check, shutting down", ex );
            shutdown = true;
        }
        if ( shutdown ) {
            bootstrap.shutdown();
        }
    }
}
