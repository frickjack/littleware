/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth;

import java.rmi.Remote;
import java.util.*;
import java.util.logging.Logger;

import littleware.asset.*;
import littleware.asset.client.AssetManagerService;
import littleware.asset.client.AssetSearchService;
import littleware.asset.client.LittleService;
import littleware.base.*;
import littleware.base.stat.*;
import littleware.security.*;
import littleware.security.auth.client.SessionHelperService;

/**
 * Enumerate different types of services exported by the 
 * SessionHelper, and provide
 * factory methods to create appropriate (uninitialized)
 * smart-proxy to support the service.
 *
 * A user should be able to introduce a new ServiceType to the
 * by taking advantage of the littleware OSGi bootstrap process.
 * Need to add documentation on that later.
 *
 * Note: subtypes implementing ServiceType must be in a code-base
 *     granted AccessPermission "littleware.security.resource.newtype"
 * Note: each service-type should implement its own unique interface
 *     to simplify the construction of Guice injection modules that key
 *     on ServiceType like littleware.security.auth.ClientServiceModule
 */
public class ServiceType<T extends LittleService> extends DynamicEnum<ServiceType> {

    private static final Logger olog = Logger.getLogger( ServiceType.class.getName () );
    private static final long serialVersionUID = 6228723237823231943L;
    private Sampler ostat_call = new SimpleSampler();
    private Class<T>  oclass_service = null;


    /**
     * Do-nothing constructor intended for deserialization only.
     */
    protected ServiceType() {
    }

    /**
     * Inject u_id, s_name, and service class
     * for the default implementation of getObjectId(), getName(),
     * and getServiceClass.
     */
    public ServiceType(UUID u_id, String s_name, Class<T> class_service ) {
        super(u_id, s_name, ServiceType.class, new AccessPermission("newtype"));
        oclass_service = class_service;
    }

    public Class<T>  getInterface() {
        return oclass_service;
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
     * Get the call-stats sampler associated with this ServiceType.
     * Currently only used on littleware server, but may
     * add some client-side support later.
     */
    public Sampler getCallSampler() {
        return ostat_call;
    }

            
    public static final ServiceType<AssetManagerService> ASSET_MANAGER =
            new ServiceType<AssetManagerService>(UUIDFactory.parseUUID("FD4C5F5B4C904AC6BDC9ECA891C39543"),
            "littleware.ASSET_MANAGER_SERVICE", AssetManagerService.class );

    public static final ServiceType<AssetSearchService> ASSET_SEARCH =
            new ServiceType<AssetSearchService>(UUIDFactory.parseUUID("56A05693C0874780A716DEFA4E262F6F"),
            "littleware.ASSET_SEARCH_SERVICE", AssetSearchService.class );

    public static final ServiceType<SessionHelperService> SESSION_HELPER =
            new ServiceType<SessionHelperService>(UUIDFactory.parseUUID("BD4110EF7A3C482D9B3500DFC74829DE"),
            "littleware.SESSION_HELPER_SERVICE", SessionHelperService.class );

    public static final ServiceType<AccountManager> ACCOUNT_MANAGER =
            new ServiceType<AccountManager>(UUIDFactory.parseUUID("402DD983DD8C47118232285E430611C2"),
            "littleware.ACCOUNT_MANAGER_SERVICE", AccountManager.class );
}

