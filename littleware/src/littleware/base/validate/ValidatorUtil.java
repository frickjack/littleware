/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
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

/**
 * Just some validation related utilities
 */
public class ValidatorUtil {

    /**
     * Little validation helper to build up a sequence of error strings
     */
    public interface Helper {

        /**
         * Add message to the internal error list if test is not true
         *
         * @param test should be true
         * @param message error if test is false
         * @return test
         */
        public boolean assume(boolean test, String message);

        /**
         * Same as assume, but returns this instead of test
         *
         * @return this
         */
        public Helper check(boolean test, String message);

        /**
         * Get an immutable copy of the internal error list
         */
        public ImmutableList<String> getErrors();

        /**
         * Return true if error list is not empty
         */
        public boolean hasErrors();
    }

    private static final class ErrorTracker implements Helper {

        private final Collection<String> errors = new ArrayList<String>();

        @Override
        public boolean assume(boolean test, String message) {
            if (!test) {
                errors.add(message);
            }
            return test;
        }

        @Override
        public Helper check(boolean test, String message) {
            assume(test, message);
            return this;
        }

        @Override
        public ImmutableList<String> getErrors() {
            return ImmutableList.copyOf(errors);
        }

        @Override
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    /**
     * Allocate a new helper 
     */
    public static Helper helper() {
        return new ErrorTracker();
    }

    /**
     * Check the test, throw ValidationException(message) if false
     *
     * @param test
     * @param message
     * @throws ValidationException
     */
    public static void check(boolean test, String message) throws ValidationException {
        if (!test) {
            throw new ValidationException(message);
        }
    }
}
