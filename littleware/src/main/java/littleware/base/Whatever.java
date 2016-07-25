package littleware.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Little utility class.  Implemented as singleton - retrieve with
 * Whatever.get()
 */
public class Whatever {
    private static final Logger log = Logger.getLogger(Whatever.class.getName() );
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final Charset UTF8 = Charset.forName( "UTF-8" );

    protected Whatever() {}
    private static final Whatever singleton = new Whatever();

    public static Whatever get() {
        return singleton;
    }

    /**
     * Close function does null check and swallows exception
     */
    public void close( InputStream stream ) {
        if ( null != stream ) {
            try {
                stream.close();
            } catch ( Throwable ex ) {}
        }
    }

    public void close( OutputStream stream ) {
        if ( null != stream ) {
            try {
                stream.close();
            } catch ( Throwable ex ) {}
        }
    }
    public void close( Reader stream ) {
        if ( null != stream ) {
            try {
                stream.close();
            } catch ( Throwable ex ) {}
        }
    }
    public void close( Writer stream ) {
        if ( null != stream ) {
            try {
                stream.close();
            } catch ( Throwable ex ) {}
        }
    }


    
    /** (null == sIn) || sIn.equals( "" ) */
    public boolean empty(String sIn) {
        return (null == sIn) || sIn.trim().equals("");
    }

    

    /**
     * Little null-aware equals method.
     *
     * @return true if x_a.equals( x_b ) or (x_a == x_b == null) -
     *          does not throw NullException
     */
    public boolean equalsSafe(Object x_a, Object x_b) {
        return (((null == x_a) && (null == x_b)) || ((null != x_a) && (null != x_b) && x_a.equals(x_b)));
    }

    /**
     * Little null-aware equals method
     *
     * @return true if x_a.equals( x_b ) and not null
     */
    public boolean equalsSafeNotNull(Object x_a, Object x_b) {
        return ((null != x_a) && (null != x_b) && x_a.equals(x_b));
    }

    /**
     * Read everything out of the given reader, and return the String.
     * Caller maintains responsiblity for closing the reader.
     *
     * @param read_all reader to suck dry
     * @return string pulled from reader
     * @throws IOException if some goes wrong
     */
    public String readAll(Reader read_all) throws IOException {
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
    public String getStackTrace() {
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
    public String join(char cJoin, String... vJoin) {
        StringBuilder sb = new StringBuilder();
        for (String sJoin : vJoin) {
            sb.append(cJoin).append(sJoin);
        }
        return sb.toString();
    }

    public <T extends Enum<T>> Optional<T> findEnumIgnoreCase(String lookFor, T[] values) {
        for (T scan : values) {
            if (lookFor.equalsIgnoreCase(scan.toString())) {
                return Optional.of(scan);
            }
        }
        return Optional.empty();
    }

    /**
     * Call the given action on the Swing dispatch thread.
     * Just invokes call directly if already on dispatch thread -
     * otherwise dispatches with event barrier.
     *
     * @throws IllegalStateException of call throws exception
     */
    public <T> T callOnSwingDispatcher( final Callable<T> call ) {
        if ( SwingUtilities.isEventDispatchThread() ) {
            log.log( Level.FINE, "Running on dispatch thread" );
            try {
                return call.call();
            } catch ( Exception ex ) {
                throw new IllegalStateException( "Failed call", ex );
            }
        }
        final IllegalStateException failure = new IllegalStateException( "Dispatch failed" );
        final CompletableFuture<T> barrier = new CompletableFuture<>();
        SwingUtilities.invokeLater(() -> {
            try {
                barrier.complete( call.call() );
            } catch ( Throwable ex ) {
                log.log( Level.WARNING, "Dispatcher call failed", ex );
                barrier.completeExceptionally(ex);
            } 
        });
        try {
            log.log( Level.FINE, "Waiting on barrier" );
            final T result = barrier.get();
            if ( result == failure ) {
                throw failure;
            }
            return (T) result;
        } catch (RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex) {
            throw new IllegalStateException( "Swing dispatch caught", ex );
        }
    }

    /**
     * Commonly accessed folders:
     * Home.getFile - shortcut for user.home,
     * Docs.getFile - shortcut for user.home/Documents|My Documents if either exists, else Home,
     * LittleHome - shortcut for PropertiesLoader.get.getLittleHome.getOr( home ),
     * Temp - shortcut for java.io.tmpdir
     */
    public enum Folder {
        Home {
            private final File folder = new File( System.getProperty( "user.home" ) );
            @Override
            public File getFolder() {
                return folder;
            }
        },
        Docs {
            private final File folder;
            {
                final File home = new File( System.getProperty( "user.home" ) );
                final File docs = new File( home, "Documents" );
                if ( docs.exists() && docs.isDirectory() ) {
                    folder = docs;
                } else {
                    final File myDocs = new File( home, "My Documents" );
                    if ( myDocs.exists() && myDocs.isDirectory() ) {
                        folder = myDocs;
                    } else {
                        folder = home;
                    }
                }
            }
            @Override
            public File getFolder() {
                return folder;
            }
        },
        LittleHome {
            private final File folder = new File( System.getProperty( "user.home" ) );
            @Override
            public File getFolder() {
                return PropertiesLoader.get().getLittleHome().orElse(folder);
            }
        },
        Temp {
            private final File folder = new File( System.getProperty( "java.io.tmpdir" ) );
            @Override
            public File getFolder() {
                return folder;
            }
        };

        public abstract File getFolder();
    }
}
