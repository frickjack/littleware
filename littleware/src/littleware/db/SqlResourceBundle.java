package littleware.db;

import java.security.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.PropertiesLoader;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.AccessPermission;

/**
 * ResourceBundle under littleware.db package
 */
public class SqlResourceBundle extends ListResourceBundle {

    private static final Logger ox_logger = Logger.getLogger(SqlResourceBundle.class.getName());

    public enum Content {

        LittlewareConnectionFactory;

        public void set(Object x_content) {
            ov_contents[ordinal()][1] = x_content;
        }
    }
    private static final Object[][] ov_contents = {
        // Let's just hard-code the connection factory for now
        {Content.LittlewareConnectionFactory.toString(), null},
    };
    

    static {
        try {
            Properties prop_jdbc = PropertiesLoader.get().loadProperties("littleware_jdbc.properties");

            ConnectionFactory x_factory =
                    new ProxoolConnectionFactory(prop_jdbc.getProperty("driver"),
                    prop_jdbc.getProperty("url"),
                    prop_jdbc.getProperty("name"),
                    prop_jdbc.getProperty("password"),
                    "littleware_user_pool",
                    Integer.parseInt(prop_jdbc.getProperty("max_connections")));
            Content.LittlewareConnectionFactory.set(new GuardedObject(x_factory,
                    new AccessPermission("littleware_user_connection_factory")));
        } catch (Exception e) {
            ox_logger.severe("Failure littleware.db resources: " + e + ", " +
                    BaseException.getStackTrace(e));
            throw new AssertionFailedException("Failed to initialize database, caught: " + e, e);
        }
    }

    /** Do nothing constructor */
    public SqlResourceBundle() {
    }

    /**
     * Implements ListResourceBundle's one abstract method -
     * ListResourceBundle takes care of the rest of the ResourceBundle interface.
     */
    public Object[][] getContents() {
        return ov_contents;
    }
    private static SqlResourceBundle obundle_singleton = null;

    /**
     * Convenience method for server-side clients
     * that can import this class.
     */
    public static SqlResourceBundle getBundle() {
        if (null != obundle_singleton) {
            return obundle_singleton;
        }

        obundle_singleton = new SqlResourceBundle(); //(SqlResourceBundle) ResourceBundle.getBundle("littleware.db.SqlResourceBundle");
        return obundle_singleton;
    }

    /**
     * Provide a Content based getObject method
     */
    public Object getObject(Content n_content) {
        return getObject(n_content.toString());
    }
}// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

