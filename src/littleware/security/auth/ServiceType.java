package littleware.security.auth;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.security.AccessController;
import java.security.Permission;
import java.security.GeneralSecurityException;
import javax.security.auth.*;
import java.lang.reflect.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.base.stat.*;
import littleware.security.*;

/**
 * Enumerate different types of services exported by the 
 * SessionHelper, and provide
 * factory methods to create appropriate (uninitialized)
 * Remote object or smart-proxy to support the service.
 *
 * A user should be able to introduce a new ServiceType to the
 * system by adding code to the classpath that defines the
 * ServiceType and service manager for that new type,
 * and adding that class to the list of classes loaded
 * by the AssetResourceBundle at startup time in the
 * bundle config file.  The manager returned by the
 * factory method should be setup to access the session's asset
 * at method invocation time to verify that the session has not
 * expired or been made ReadOnly.  The littleware.security.auth.SessionInvocationHandler
 * facilitates this.
 *
 * Note: subtypes implementing ServiceType must be in a code-base
 *     granted AccessPermission "littleware.security.resource.newtype"
 * Note: each service-type should implement its own unique interface
 *     to simplify the construction of Guice injection modules that key
 *     on ServiceType like littleware.security.auth.ClientServiceModule
 */
public abstract class ServiceType<T extends Remote> extends DynamicEnum<ServiceType> implements ServiceProviderFactory<T> {

    private static Logger olog_generic = Logger.getLogger("littleware.security.auth.ServiceType");
    private static Logger olog_call = Logger.getLogger("littleware.security.auth.ServiceType.call_logger");
    private Sampler ostat_call = new SimpleSampler();

    /**
     * Do-nothing constructor intended for deserialization only.
     */
    protected ServiceType() {
    }

    /**
     * Constructor for subtypes to register a u_id/s_name
     * for the default implementation of getObjectId() and getName()
     */
    protected ServiceType(UUID u_id, String s_name) {
        super(u_id, s_name, ServiceType.class, new AccessPermission("newtype"));
    }

    /** Shortcut to DynamicEnum.getMembers */
    public static Set<ServiceType> getMembers() {
        return getMembers(ServiceType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static ServiceType getMember(UUID u_id) throws NoSuchThingException {
        return getMember(u_id, ServiceType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static ServiceType getMember(String s_name) throws NoSuchThingException {
        return getMember(s_name, ServiceType.class);
    }

    /**
     * Get the call-stats sampler associated with this ServiceType
     */
    public Sampler getCallSampler() {
        return ostat_call;
    }

    /**
     * Get the logger to write call-trace to
     */
    public Logger getCallLogger() {
        return olog_call;
    }

    /**
     * Get the Class of the interface this service provider supports: T.class
     */
    public abstract Class<T> getServiceInterface();
    static AssetSearchManager om_search = null;

    /** 
     * Server-side only function.
     * Just expose it here to make it easy for 3rd parties to dynamically add services.
     * Subtypes should override this with a final method for security reasons.
     */
    public abstract T createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
    static ServiceProviderFactory<AssetManager> ofactory_asset_manager = null;
    static ServiceProviderFactory<AssetSearchManager> ofactory_search_manager = null;
    static ServiceProviderFactory<AccountManager> ofactory_account_manager = null;
    static ServiceProviderFactory<AclManager> ofactory_acl_manager = null;
    public static final ServiceType<AssetManager> ASSET_MANAGER =
            new ServiceType<AssetManager>(UUIDFactory.parseUUID("FD4C5F5B4C904AC6BDC9ECA891C39543"),
            "littleware.ASSET_MANAGER_SERVICE") {

                public AssetManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException,
                        GeneralSecurityException, RemoteException {
                    if (null == ofactory_asset_manager) {
                        ResourceBundle bundle_asset = ResourceBundle.getBundle("littleware.asset.server.AssetResourceBundle");
                        ofactory_asset_manager = (ServiceProviderFactory<AssetManager>) bundle_asset.getObject("AssetManagerServiceProvider");
                    }
                    return ofactory_asset_manager.createServiceProvider(m_helper);
                }

                public Class<AssetManager> getServiceInterface() {
                    return AssetManager.class;
                }
            };
    public static final ServiceType<AssetSearchManager> ASSET_SEARCH =
            new ServiceType<AssetSearchManager>(UUIDFactory.parseUUID("56A05693C0874780A716DEFA4E262F6F"),
            "littleware.ASSET_SEARCH_SERVICE") {

                public AssetSearchManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException,
                        GeneralSecurityException, RemoteException {
                    if (null == ofactory_search_manager) {
                        ResourceBundle bundle_asset = ResourceBundle.getBundle("littleware.asset.server.AssetResourceBundle");
                        ofactory_search_manager = (ServiceProviderFactory<AssetSearchManager>) bundle_asset.getObject("AssetSearchServiceProvider");
                    }
                    return ofactory_search_manager.createServiceProvider(m_helper);
                }

                public Class<AssetSearchManager> getServiceInterface() {
                    return AssetSearchManager.class;
                }
            };
    public static final ServiceType<SessionHelper> SESSION_HELPER =
            new ServiceType<SessionHelper>(UUIDFactory.parseUUID("BD4110EF7A3C482D9B3500DFC74829DE"),
            "littleware.SESSION_HELPER_SERVICE") {

                /** Pass-through - just return the m_helper argument */
                public SessionHelper createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException,
                        GeneralSecurityException, RemoteException {
                    return m_helper;
                }

                public Class<SessionHelper> getServiceInterface() {
                    return SessionHelper.class;
                }
            };
    public static final ServiceType<AccountManager> ACCOUNT_MANAGER =
            new ServiceType<AccountManager>(UUIDFactory.parseUUID("402DD983DD8C47118232285E430611C2"),
            "littleware.ACCOUNT_MANAGER_SERVICE") {

                public AccountManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException,
                        GeneralSecurityException, RemoteException {
                    if (null == ofactory_account_manager) {
                        ResourceBundle bundle_security = ResourceBundle.getBundle("littleware.security.server.SecurityResourceBundle");
                        ofactory_account_manager = (ServiceProviderFactory<AccountManager>) bundle_security.getObject("AccountServiceProvider");
                    }
                    return ofactory_account_manager.createServiceProvider(m_helper);
                }

                public Class<AccountManager> getServiceInterface() {
                    return AccountManager.class;
                }
            };
    public static final ServiceType<AclManager> ACL_MANAGER =
            new ServiceType<AclManager>(UUIDFactory.parseUUID("25A9379640B94B26BBA6D0607981B070"),
            "littleware.ACL_MANAGER_SERVICE") {

                public AclManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException,
                        GeneralSecurityException, RemoteException {
                    if (null == ofactory_acl_manager) {
                        ResourceBundle bundle_security = ResourceBundle.getBundle("littleware.security.server.SecurityResourceBundle");
                        ofactory_acl_manager = (ServiceProviderFactory<AclManager>) bundle_security.getObject("AclServiceProvider");
                    }
                    return ofactory_acl_manager.createServiceProvider(m_helper);
                }

                public Class<AclManager> getServiceInterface() {
                    return AclManager.class;
                }
            };
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

