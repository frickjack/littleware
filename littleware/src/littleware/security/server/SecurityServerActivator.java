/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.server;

import com.google.inject.Inject;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.security.AccountManager;
import littleware.security.AclManager;
import littleware.security.SecurityAssetType;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi activator for littleware.security bundle -
 * just registers the AssetSpecializers for the SecurityAssetTypes
 * and the security services with the LittleRegistry.
 */
public class SecurityServerActivator implements BundleActivator {

    /**
     * Constructor registers the appropriate handlers for the
     * SecurityAssetType statics.
     * 
     * @param registry
     * @param mgr_account is cast to AssetSpecializer
     * @param mgr_acl is cast to AssetSpecializer
     */
    @Inject
    public SecurityServerActivator ( AssetSpecializerRegistry registry,
            AccountManager mgr_account, AclManager mgr_acl
            )
    {
        registry.registerService( SecurityAssetType.PRINCIPAL, (AssetSpecializer) mgr_account );
        registry.registerService( SecurityAssetType.GROUP, (AssetSpecializer) mgr_account );
        registry.registerService( SecurityAssetType.QUOTA, (AssetSpecializer) mgr_account );
        registry.registerService( SecurityAssetType.USER, (AssetSpecializer) mgr_account );
        registry.registerService( SecurityAssetType.ACL, (AssetSpecializer) mgr_acl );
    }

    /**
     * NOOP for now - might want to later setup a littleware-authenticate
     * service in JNDI.
     */
    public void start(BundleContext ctx) throws Exception {
    }

    /** NOOP for now */
    public void stop(BundleContext ctx) throws Exception {
    }

}
