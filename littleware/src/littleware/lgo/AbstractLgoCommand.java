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
import littleware.base.feedback.Feedback;


/**
 * Handy LgoCommand base class
 */
public abstract class AbstractLgoCommand<Tin,Tout> implements LgoCommand {
    private static final Logger log = Logger.getLogger( AbstractLgoCommand.class.getName() );

    private final Tin input;

    /**
     * Subtypes must initialize the name property.
     * 
     * @param name
     */
    protected AbstractLgoCommand( String name, Tin input ) {
        this.name = name;
        this.input = input;
    }
            
    private final String name;
    @Override
    public String getName() {
        return name;
    }

    public Tin getInput() { return input; }


    /**
     * Abstract implementation just calls through to
     *     runCommand.toString()
     * Override if that is not appropriate for your command.
     */
    @Override
    public String runCommandLine( Feedback feedback ) throws Exception
    {
        return runCommand( feedback ).toString();
    }

    @Override
    public abstract Tout runCommand( Feedback feedback ) throws Exception;
}
