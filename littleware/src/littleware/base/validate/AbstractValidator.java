/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.validate;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import littleware.base.Whatever;


/**
 * Base implementation for a compound validator
 */
public abstract class AbstractValidator implements Validator {
    protected static final class ErrorTracker {
        private final Collection<String> errors = new ArrayList<String>();

        private ErrorTracker() {}

        /**
         * Add message to the internal error list if test is not true
         *
         * @param test should be true
         * @param message error if test is false
         * @return test
         */
        public boolean assume( boolean test, String message ) {
            if( ! test ) {
                errors.add( message );
            }
            return test;
        }

        /**
         * Same as assume, but returns this instead of test
         *
         * @return this
         */
        public ErrorTracker check( boolean test, String message ) {
            assume( test, message );
            return this;
        }

        /**
         * Get an immutable copy of the internal error list
         */
        public ImmutableList<String> getErrors() {
            return ImmutableList.copyOf( errors );
        }

        /**
         * Return true if error list is not empty
         */
        public boolean hasErrors() { return ! errors.isEmpty(); }
    }

    protected ErrorTracker buildErrorTracker() {
        return new ErrorTracker();
    }

    @Override
    public final void validate() throws ValidationException {
        final Collection<String> errors = checkIfValid();
        if ( ! errors.isEmpty() ) {
            final StringBuilder sb = new StringBuilder();
            for( String message : errors ) {
                sb.append( message ).append( Whatever.NEWLINE );
            }
            throw new ValidationException( sb.toString() );
        }
    }
}
