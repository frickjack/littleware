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

import littleware.lgo.LgoCommandLine;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.AssertionFailedException;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.LoggerFeedback;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.helper.RunnerActivator;

/**
 * Activator for lgo pipe processor
 */
public class LgoPipeActivator extends RunnerActivator {

    private static final Logger log = Logger.getLogger(LgoPipeActivator.class.getName());
    private final LittleBootstrap bootstrap;
    private final LgoCommandLine lgoCL;

    @Inject
    public LgoPipeActivator(LgoCommandLine lgoCL,
            LittleBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.lgoCL = lgoCL;
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
        System.out.println("LGO>>");
        for (String sLine = reader.readLine(); null != sLine; sLine = reader.readLine()) {
            if (Thread.interrupted()) {
                log.log(Level.INFO, "LGO processing thread interrupted - exiting loop");
                return;
            }
            String sClean = sLine.trim();
            if (sClean.length() == 0) {
                continue;
            }
            String sCommand = sClean;
            final List<String> commandTokens = new ArrayList<String>();
            String sArg = "";
            final int iFirstSpace = sClean.indexOf(" ");

            if (iFirstSpace > 0) {
                sCommand = sClean.substring(0, iFirstSpace);
                sClean = sClean.substring(iFirstSpace);

                // -- marks end of argumnts, pass rest of string as input-string
                final int iDashDash = sClean.indexOf(" -- ");
                if (iDashDash >= 0) {
                    sArg = sClean.substring(iDashDash + 3).trim();
                    sClean = sClean.substring(0, iDashDash).trim();
                }
                // TODO - add some smarter parsing
                for (String sProcess : sClean.split("\\s+")) {
                    if (sProcess.trim().length() > 0) {
                        if (sProcess.indexOf("%") < 0) {
                            commandTokens.add(sProcess);
                        } else {
                            commandTokens.add(URLDecoder.decode(sProcess, "UTF8"));
                        }
                    }
                }
            }
            if (sCommand.equalsIgnoreCase("exit")) {
                break;
            }
            lgoCL.processCommand(sCommand, commandTokens, sArg, feedback);
            System.out.println("LGO>>");
        }
    }

    @Override
    public void run() {
        try {
            processPipe( new LoggerFeedback() );
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed command, caught: " + e, e);
        } finally {
            bootstrap.shutdown();
        }

    }
}
