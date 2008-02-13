package littleware.asset.server;

import java.rmi.RemoteException;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.GuardedObject;
import java.sql.DriverManager;
import java.sql.Connection;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.io.IOException;
import littleware.asset.*;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.server.db.DbCacheManager;
import littleware.asset.server.db.postgres.DbAssetPostgresManager;
import littleware.asset.server.db.derby.DerbyDbCacheManager;
import littleware.asset.server.SimpleAssetManager;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.base.Factory;
import littleware.base.PropertiesLoader;
import littleware.base.AssertionFailedException;
import littleware.base.AccessPermission;
import littleware.db.SqlResourceBundle;
import littleware.security.AccountManager;
import littleware.security.GetGuardedResourceAction;
import littleware.security.auth.ServiceProviderFactory;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.server.AbstractServiceProviderFactory;
import littleware.security.auth.server.db.DbAuthManager;
import littleware.security.auth.server.db.postgres.PostgresDbAuthManager;
import littleware.security.server.SimpleAccountManager;

/**
 * Resource bundle under littleware.asset package
 */
public class AssetResourceBundle extends ListResourceBundle {

    private static final Logger olog_generic = Logger.getLogger(AssetResourceBundle.class.getName());
    private static boolean ob_initialized = false;
    private static AssetManager om_asset = null;

    /** Internal convenience for lookups into ov_contents */
    public enum Content {

        AssetDbManager, AssetRetriever, AssetManager, CacheConnection, AssetSearcher, CacheManager, CacheDbManager, AssetManagerServiceProvider, AssetSearchServiceProvider, AccountManager, AuthDbManager;

        /** Internal utility */
        void set(Object x_value) {
            ov_contents[ordinal()][1] = x_value;
        }
    }
    private static final Object[][] ov_contents = new Object[Content.values().length][2];

    static {
        for (Content n_content : Content.values()) {
            ov_contents[n_content.ordinal()][0] = n_content.toString();
            n_content.set(null);
        }
    }

    /** Do nothing constructor */
    public AssetResourceBundle() {
        super();
    }

    private static synchronized void initBundle() {
        if (ob_initialized) {
            return;
        }

        // Load properties file to get size of cache - whatever
        Properties prop_database = null;
        try {
            Properties prop_database_default = new Properties();

            prop_database_default.put("derby_workarea", "/tmp/littleware");
            prop_database = PropertiesLoader.loadProperties("littleware_jdbc.properties", prop_database_default);
        } catch (IOException e) {
            olog_generic.log(Level.SEVERE, "Failed properties load, caught: " + e);
            throw new AssertionFailedException("Failed to load jdbc props, caught: " + e, e);
        }
        try {
            // Register our drivers
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            Date t_now = new Date();
            String s_connection_url = "jdbc:derby:directory:" + prop_database.getProperty("derby_workarea") + "/" + t_now.getTime();
            String s_derby_template = prop_database.getProperty("derby_template");
            if (null == s_derby_template) {
                throw new AssertionFailedException("derby_template not defined in littleware_jdbc.properties");
            }

            olog_generic.log(Level.INFO, "Derby connection up");

            Connection sqlconn_derby = DriverManager.getConnection(s_connection_url + ";createFrom=" + s_derby_template);
            DbCacheManager m_dbcache = new DerbyDbCacheManager();
            // Constructor sets up singleton here
            SimpleCacheManager m_cache = new SimpleCacheManager(sqlconn_derby, m_dbcache);

            Content.CacheConnection.set(new GuardedObject(sqlconn_derby, new AccessPermission("generic")));

            Content.CacheManager.set(new GuardedObject(SimpleCacheManager.getTheManager(), new AccessPermission("cache_manager")));
            Content.CacheDbManager.set(new GuardedObject(m_dbcache, new AccessPermission("cache_dbmanager")));

            {
                final PrivilegedAction action_getresource = new GetGuardedResourceAction(SqlResourceBundle.getBundle(), SqlResourceBundle.Content.LittlewareConnectionFactory.toString());
                final DataSource sql_pool = (DataSource) AccessController.doPrivileged(action_getresource);
                SimpleLittleTransaction.setDataSource(sql_pool);
            }

            // Id of this database client for interclient cache management
            int i_database_client = Integer.parseInt(prop_database.getProperty("database_client_id"));
            final DbAssetManager m_dbasset = new DbAssetPostgresManager(i_database_client);
            final AssetRetriever m_retriever = new LocalAssetRetriever(m_dbasset, m_cache);
            final AssetSearchManager m_searcher = new SimpleAssetSearchManager(m_dbasset, m_cache);

            om_asset = new SimpleAssetManager(m_cache, m_retriever, m_dbasset, null);

            Content.AssetRetriever.set(new GuardedObject(m_retriever, new AccessPermission("general")));
            Content.AssetManager.set(om_asset);
            Content.AssetDbManager.set(new GuardedObject(m_dbasset, new AccessPermission("asset_dbmanager")));



            //
            // Launch background thread that tries to keep our cache in sync with the database
            // in the presence of other servers changing the repository
            //
            m_dbasset.launchCacheSyncThread(m_cache);
            Content.AssetSearcher.set(m_searcher);

            final DbAuthManager m_dbauth = new PostgresDbAuthManager();
            final AccountManager m_account = new SimpleAccountManager(om_asset, m_searcher, m_dbauth);

            // Inject circular dependency here
            Content.AccountManager.set(m_account);
            ((SimpleAssetManager) om_asset).setAccountManager(m_account);

            Content.AuthDbManager.set(new GuardedObject(m_dbauth, new AccessPermission("generic")));

            Content.AssetManagerServiceProvider.set(new AbstractServiceProviderFactory<AssetManager>(ServiceType.ASSET_MANAGER, m_searcher) {

                public AssetManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                    AssetManager m_proxy = checkAccessMakeProxy(m_helper, false, om_asset);
                    return new RmiAssetManager(m_proxy);
                }
            });

            Content.AssetSearchServiceProvider.set(new AbstractServiceProviderFactory<AssetSearchManager>(ServiceType.ASSET_SEARCH, m_searcher) {

                public AssetSearchManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                    AssetSearchManager m_proxy = checkAccessMakeProxy(m_helper, true, m_searcher);
                    return new RmiSearchManager(m_proxy);
                }
            });

            ob_initialized = true;
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Couldn't find driver: " + e);
            throw new AssertionFailedException("Failed to initialize core server bundle", e);
        }


        /**
         * Now try to make sure that our varous AssetTypes are loaded an initialized.
         * Set this up to key off a properties file later.
         */
        Set<String> v_load_classes = new HashSet<String>();
        {
            String[] v_always = {"littleware.asset.AssetType", "littleware.security.SecurityAssetType"};

            Collections.addAll(v_load_classes, v_always);
        }
        try {
            Properties prop_user = PropertiesLoader.loadProperties("littleware.properties", new Properties());
            String s_user_classes = prop_user.getProperty("bootstrap_classes");

            if (null != s_user_classes) {
                try {
                    StringTokenizer tok_classlist = new StringTokenizer(s_user_classes, " :;");
                    for (String s_token = null; tok_classlist.hasMoreTokens();) {
                        s_token = tok_classlist.nextToken();
                        v_load_classes.add(s_token);
                        olog_generic.log(Level.INFO, "Adding littleware.properties class to bootstrap classes: " + s_token);
                    }
                } catch (NoSuchElementException e) {
                    olog_generic.log(Level.WARNING, "Failure parsing bootclass properties, caught: " + e);
                }
            }
        } catch (IOException e) {
            olog_generic.log(Level.WARNING, "Failure loading littleware.properties, caught: " + e);
        }

        for (String s_name : v_load_classes) {
            try {
                olog_generic.log(Level.INFO, "Loading class: " + s_name);
                Class.forName(s_name);
            } catch (Exception e) {
                olog_generic.log(Level.WARNING, "Failed to load class: " + s_name + ", caught: " + e);
            } catch (ExceptionInInitializerError e) {
                olog_generic.log(Level.SEVERE, "Failed to load class: " + s_name + ", caught: " + e + " with cause: " + e.getCause());
                throw e;
            }
        }
    }

    /**
     * Implements ListResourceBundle's one abstract method -
     * ListResourceBundle takes care of the rest of the ResourceBundle interface.
     */
    public Object[][] getContents() {
        if (!ob_initialized) {
            initBundle();
        }
        return ov_contents;
    }
    private static AssetResourceBundle obundle_singleton = null;

    /**
     * Convenience method for server-side clients
     * that can import this class.
     */
    public static AssetResourceBundle getBundle() {
        if (null != obundle_singleton) {
            return obundle_singleton;
        }

        obundle_singleton = (AssetResourceBundle) ResourceBundle.getBundle("littleware.asset.server.AssetResourceBundle");
        return obundle_singleton;
    }

    /**
     * Provide a Content based getObject method
     */
    public Object getObject(Content n_content) {
        return getObject(n_content.toString());
    }

    /**
     * Shortcut for easy access to an AccountManager
     */
    public static AssetManager getAssetManager() {
        if (!ob_initialized) {
            initBundle();
        }
        return om_asset;
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
