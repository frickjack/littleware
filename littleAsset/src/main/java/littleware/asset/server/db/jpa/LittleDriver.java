/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;
import littleware.base.Whatever;
import littleware.bootstrap.LittleBootstrap;


/**
 * JDBC driver that just delegates to the active DataSource defined in the
 * active littleware runtime.
 * Register this driver with JPA persistence.xml to plug into the
 * littleware managed DataSource.
 */
public class LittleDriver implements Driver {
    private static  DataSource dataSource = null;
    /**
     * HibernateProvider injects littleware data source at startup time as needed
     */
    public static void setDataSource( DataSource value ) {
        dataSource = value;
    }
    
    
    @Override
    public Connection connect(String string, Properties prprts) throws SQLException {
        Whatever.get().check( "LittleDriver requires data source injection",  null != dataSource );
        return dataSource.getConnection();
    }

    @Override
    public boolean acceptsURL(String string) throws SQLException {
        return true;
    }

    private static final DriverPropertyInfo[] empty = new DriverPropertyInfo[0];
    @Override
    public DriverPropertyInfo[] getPropertyInfo(String string, Properties prprts) throws SQLException {
        return empty;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    private static final Logger log = Logger.getLogger( LittleDriver.class.getName() );
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return log;
    }
    
}
