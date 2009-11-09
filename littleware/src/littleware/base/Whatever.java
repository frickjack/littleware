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

import java.io.IOException;
import java.io.Reader;

/**
 * Little assertion class.
 */
public abstract class Whatever {

    /** (null == sIn) || sIn.equals( "" ) */
    public static boolean empty(String sIn) {
        return (null == sIn) || sIn.trim().equals("");
    }

    /**
     * Throw a runtime AssetionFaledException if b_assert evaluates false.
     *
     * @param s_message associated with this check
     * @param b_assert set true if all is ok, false if assertion failed
     */
    public static void check(String s_message, boolean b_assert) {
        if (!b_assert) {
            throw new AssertionFailedException(s_message);
        }
    }

    /**
     * Little null-aware equals method.
     *
     * @return true if x_a.equals( x_b ) or (x_a == x_b == null) -
     *          does not throw NullException
     */
    public static boolean equalsSafe(Object x_a, Object x_b) {
        return (((null == x_a) && (null == x_b)) || ((null != x_a) && (null != x_b) && x_a.equals(x_b)));
    }

    /**
     * Little null-aware equals method
     *
     * @return true if x_a.equals( x_b ) and not null
     */
    public static boolean equalsSafeNotNull(Object x_a, Object x_b) {
        return ((null != x_a) && (null != x_b) && x_a.equals(x_b));
    }

    /**
     * Read everything out of the given reader, and return the String.
     * Caller maintains responsiblity for closing the reader.
     *
     * @param read_all reader to suck dry
     * @return string pulled from reader
     * @exception IOException if something goes wrong
     */
    public static String readAll(Reader read_all) throws IOException {
        final int i_buffer = 10240;
        char[] v_buffer = new char[i_buffer];
        StringBuilder sb_result = new StringBuilder(i_buffer);

        for (int i_in = read_all.read(v_buffer, 0, i_buffer);
                i_in > -1;
                i_in = read_all.read(v_buffer, 0, i_buffer)) {
            if (i_in > 0) {
                sb_result.append(v_buffer, 0, i_in);
            }
        }
        return sb_result.toString();
    }

    /**
     * Little utility - throws and catches an internal exception to 
     * return a String of the current stack trace.  Useful for debugging sometimes.
     *
     * @return stack trace from catching a bogus exception
     */
    public static String getStackTrace() {
        try {
            throw new Exception("Get Stack Trace");
        } catch (Exception e) {
            return BaseException.getStackTrace(e);
        }
    }

    /**
     * Build string cJoin + s1 + cJoin + s2 + ...,
     * start with cJoin, end with sLast
     * 
     * @param cJoin character to join strings with
     * @param vJoin strings to append with cJoin separator
     * @return joined string
     */
    public static String join(char cJoin, String... vJoin) {
        StringBuilder sb = new StringBuilder();
        for (String sJoin : vJoin) {
            sb.append(cJoin).append(sJoin);
        }
        return sb.toString();
    }

    public static <T extends Enum<T>> Maybe<T> findEnumIgnoreCase(String lookFor, T[] values) {
        for (T scan : values) {
            if (lookFor.equalsIgnoreCase(scan.toString())) {
                return Maybe.something(scan);
            }
        }
        return Maybe.empty();
    }
    public static final String NEWLINE = System.getProperty("line.separator");
}
