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

import java.util.logging.Logger;

//import gnu.getopt.Getopt;
//import gnu.getopt.LongOpt;

import java.util.List;
import littleware.base.XmlSpecial;
import littleware.base.feedback.Feedback;

/**
 * XML encode/decode LgoCommand
 */
public class XmlEncodeCommand extends AbstractLgoBuilder<String> {

    private static final Logger log = Logger.getLogger(XmlEncodeCommand.class.getName());

    @Override
    public LgoCommand buildSafe(String input) {
        return new Command( input );
    }

    @Override
    public LgoCommand buildFromArgs(List<String> args) {
        return new Command( args.get(0) );
    }

    private static class Command extends AbstractLgoCommand<String, String> {

        public Command(String input) {
            super(XmlEncodeCommand.class.getName(), input);
        }

        @Override
        public String runCommand(Feedback feedback) {
            return XmlSpecial.encode(getInput());
        }
    }

    /**
     * Constructor just sets the command-name to this.class.getName,
     * and the command-help property to the supplied help object.
     */
    public XmlEncodeCommand() {
        super(XmlEncodeCommand.class.getName());
    }

}
