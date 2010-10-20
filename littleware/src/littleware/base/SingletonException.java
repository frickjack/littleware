/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base;

/**
 * Exception thrown when multiple instances of Singleton created,
 * or on attempt to access uninitialized Singleton.
 */
public class SingletonException extends BaseRuntimeException {

    /** Default constructor */
    public SingletonException() {
        super("Exception in littleware.base package");
    }

    /** With message */
    public SingletonException(String s_message) {
        super(s_message);
    }

    /** Exception cascading */
    public SingletonException(String s_message, Throwable e_cause) {
        super(s_message, e_cause);
    }
}

