package littleware.db;

import java.sql.*;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import littleware.base.*;

/**
 * Little connection-factory wrapper around Proxool connection pool.
 * Adds a couple of behaviors.
 * <ul>
 *   <li> Block when reach connection limit rather than throw exception </li>
 *   <li> Throw an exception if a single thread
 *              that already has an open connection does
 *              multiple checkouts </li>
 * </ul>
 */
public class ProxoolConnectionFactory implements ConnectionFactory {

    private final static Logger olog_generic = Logger.getLogger( ProxoolConnectionFactory.class.getName() );
    private final Properties ox_connection_properties = new Properties();
    private final String os_core_url;
    private final String os_proxool_url;
    private final String os_core_driver;
    private final String os_proxool_alias;
    private int oi_max_connections = 20;
    private int oi_checkout_count = 0;
    private ThreadLocal<Connection> othread_connection = new ThreadLocal<Connection>();

    /**
     * Little invocation handler that we can use to track
     * when a connection gets closed.
     * Catches the close() event, and invokes
     *          factory->recycle()
     * instead.
     */
    private class ConnectionHandler implements InvocationHandler {

        private Connection osql_conn = null;
        private boolean ob_recycled = false;

        /**
         * Stash a reference to the wrapped connection
         */
        public ConnectionHandler(Connection sql_conn) {
            osql_conn = sql_conn;
        }

        public Object invoke(Object proxy, Method method_call, Object[] v_args) throws Throwable {
            if (method_call.getName().equals("close")) {
                olog_generic.log(Level.FINE, "Proxy Connection recycling");
                try {
                    if (!ob_recycled) {
                        ob_recycled = true;
                        recycleInternal(osql_conn);
                        osql_conn = null;
                        return null;
                    } else {
                        olog_generic.log(Level.WARNING, "Closing already recycled connection!");
                    }
                } catch (FactoryException e) {
                    throw new LittleSqlException("Failed to recycle connection, caught: " + e);
                }
            }
            try {
                return method_call.invoke(osql_conn, v_args);
            } catch (IllegalAccessException e) {
                olog_generic.log(Level.INFO, "Caught unexpected: " + e);
                throw new AssertionFailedException("Illegal access: " + e, e);
            } catch (InvocationTargetException e) {
                olog_generic.log(Level.INFO, "FRICK: " + e);
                Throwable err = e.getCause();

                if (err instanceof Exception) {
                    throw (Exception) err;
                } else if (err instanceof Error) {
                    throw (Error) err;
                } else {
                    throw new AssertionFailedException("Unexpected throwable error: " + e, e);
                }
            }
        }
    }

    /**
     * Constructor takes info necessary to setup the pool
     *
     * @param s_driver like "org.postgresql.Driver
     * @param s_url like jdbc:postgresql:database
     * @param s_username like demo_user
     * @param s_password like demo_user_password
     * @param s_proxool_alias like demo_pool
     * @param i_max_connections to limit pool to - must be at least 5
     */
    public ProxoolConnectionFactory(String s_driver, String s_url, String s_username, String s_password, String s_proxool_alias, int i_max_connections) throws LittleSqlException {
        oi_max_connections = i_max_connections;
        if (oi_max_connections < 5) {
            oi_max_connections = 5;
        }
        ox_connection_properties.setProperty("user", s_username);
        ox_connection_properties.setProperty("password", s_password);
        ox_connection_properties.setProperty("proxool.minimum-connection-count", "1");
        ox_connection_properties.setProperty("proxool.maximum-connection-count", Integer.toString(oi_max_connections));

        /**... Proxool health check does not seem to work right with postgres ? ...
        if ( 0 <= s_driver.indexOf ( "oracle" ) ) {
        ox_connection_properties.setProperty("proxool.house-keeping-test-sql", "SELECT 'Hello' FROM dual");
        } else {
        ox_connection_properties.setProperty("proxool.house-keeping-test-sql", "SELECT 'Hello'");
        }
        olog_generic.log ( Level.INFO, "Setting test query to: " +
        ox_connection_properties.getProperty( "proxool.house-keeping-test-sql" ) );
        ox_connection_properties.setProperty ( "proxool.test-before-use", "true" );
         */

        os_core_url = s_url;
        os_core_driver = s_driver;
        if ((s_proxool_alias.indexOf(".") >= 0) || (s_proxool_alias.indexOf(":") >= 0)) {
            throw new LittleSqlException("Proxool alias may not contain ':' or '.' : " + s_proxool_alias);
        }
        os_proxool_alias = s_proxool_alias;
        os_proxool_url = "proxool." + os_proxool_alias + ":" + os_core_driver + ":" + os_core_url;

        try {
            // Register our drivers
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
            Class.forName(s_driver);
        } catch (ClassNotFoundException e) {
            olog_generic.log(Level.WARNING, "Couldn't find driver: " + e);
            throw new LittleSqlException("Failure loading jdbc drivers, caught: " + e);
        }
    }

    public boolean isWrapperFor( Class<?> class_check ) {
        return class_check.getName ().equals( os_core_driver );
    }
    
    
    /**
     * Type-specific alias for Factory create() method.
     *
     * @return a db connection that should be recycled back into this factory
     * @exception LittleSqlException if some problem allocating a connection
     */
    public Connection create() throws FactoryException {
        try {
            return getConnection();
        } catch (SQLException e) {
            throw new FactoryException("Failure allocating db connection, caught: " + e);
        }
    }

    /**
     * javax.sql.DataSource API
     */
    public synchronized Connection getConnection() throws SQLException {
        Connection sql_conn = othread_connection.get();

        if ((null != othread_connection.get()) && (!othread_connection.get().isClosed())) {
            try {
                throw new Exception("Get stacktrace");
            } catch (Exception e) {
                throw new LittleSqlException("Single thread attempting to checkout multiple connections from same pool: " + BaseException.getStackTrace(e));
            }
        }
        for (int i_count = 0; (i_count < 2) && (oi_checkout_count >= oi_max_connections); ++i_count) {
            olog_generic.log(Level.INFO, "Waiting on database connection");
            try {
                wait(5000);
            } catch (InterruptedException e) {
            }
        }
        if (oi_checkout_count >= oi_max_connections) {
            throw new LittleSqlException("Connection still not available after waiting");
        }

        sql_conn = DriverManager.getConnection(os_proxool_url, ox_connection_properties);
        if (sql_conn == null) {
            olog_generic.log(Level.WARNING, "Didn't get connection, which probably means that no Driver accepted the URL");
            throw new LittleSqlException("Didn't get connection, which probably means that no Driver accepted the URL");
        }
        ++oi_checkout_count;
        othread_connection.set(sql_conn);
        return (Connection) Proxy.newProxyInstance ( Connection.class.getClassLoader (),
											   new Class[] { Connection.class },
											   new ConnectionHandler ( sql_conn )
											   );
    }

    /**
     * javax.sql.DataSource API - always throws an exception as we
     * donot allow users to point the ConnectionPool at a different data source.
     */
    public Connection getConnection(String s_user, String s_password) throws SQLException {
        throw new LittleSqlException("Not implemented - use getConnection() instead");
    }

    /** javax.sql.DataSource API - always returns NULL */
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    /** javax.sql.DataSource API - implemented as a NOOP */
    public void setLogWriter(PrintWriter x_writer) throws SQLException {
    }

    /** javax.sql.DataSource API - implemented as NOOP */
    public void setLoginTimeout(int i_seconds) throws SQLException {
    }

    /** javax.sql.DataSource API - always returns 0 */
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    /**
     * Internal method shared by public recycle and by
     * Conenction wrapper-proxy close()
     */
    synchronized void recycleInternal(Connection sql_conn) throws FactoryException {
        try {
            sql_conn.close();
        } catch (SQLException e) {
            throw new FactoryException("Connection close caught: " + e, e);
        } finally {
            --oi_checkout_count;
            othread_connection.set(null);
            notify();
        }
    }

    /**
     * Close the given connection, and decrement the checkout count
     *
     * @param sql_conn is the object to recycle - should be the connection proxy
     *       returned by getConnection
     * @throws FactoryException if sql_conn does not belong to this factory
     */
    public synchronized void recycle(Connection sql_conn) throws FactoryException {
        try {
            sql_conn.close();
        } catch (SQLException e) {
            throw new FactoryException("Connection close caught: " + e, e);
        }
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.com