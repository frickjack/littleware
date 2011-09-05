/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import littleware.base.Whatever;

/**
 * Reader/writer command-line authentication handler
 */
public class CliCallbackHandler implements CallbackHandler {

    private final BufferedReader reader;
    private final Writer writer;
    private final String message;

    /**
     * Default constructor interacts with user via System.out, System.in
     */
    public CliCallbackHandler() {
        this( "" );
    }

    /**
     * Set prefix message, ex: "Login Failed, Try again"
     */
    public CliCallbackHandler( String message ) {
        this( new BufferedReader(new InputStreamReader(System.in)),
            new OutputStreamWriter(System.out), message
            );
    }

    public CliCallbackHandler( BufferedReader reader, Writer writer, String message ) {
        this.reader = reader;
        this.writer = writer;
        this.message = message;
    }

    /**
     * writer.write( prompt ); return reader.readLine()
     */
    private String getUserName() throws IOException {
        writer.write("User name: ");
        writer.flush();
        return reader.readLine();
    }

    private String getPassword() throws IOException {
        writer.write("Password: ");
        writer.flush();
        final String eol = "\r\n" + Whatever.NEWLINE;
        final StringBuilder sb = new StringBuilder();
        for (char scan = Character.toChars(reader.read())[0];
                eol.indexOf(scan) < 0;
                scan = Character.toChars(reader.read())[0]) {
            sb.append(scan);
            if (sb.length() > 100) {
                throw new IllegalStateException("Over 100 characters read");
            }
        }
        return sb.toString();
    }

    /**
     * Implement LoginModule CallbackHandler interface
     *
     * @param callbackList from a LoginModule or whatever to handle
     * @throws java.io.IOException if user cancels out of dialog
     * @throws javax.security.auth.callback.UnsupportedCallbackException for unexpected callback
     */
    @Override
    public void handle(Callback[] callbackList) throws IOException, UnsupportedCallbackException {
        writer.write( message );
        writer.write( Whatever.NEWLINE );
        for (Callback callback : callbackList) {
            if (callback instanceof TextOutputCallback) {
                writer.write(((TextOutputCallback) callback).getMessage()
                        + Whatever.NEWLINE);
            }
        }

        for (Callback callback : callbackList) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(getUserName());
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(getPassword().toCharArray());
            } else if (callback instanceof TextOutputCallback) {
                // NOOP
            } else {
                throw new UnsupportedCallbackException(callback, "Unsupported callback");
            }
        }
    }
}
