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

package littleware.apps.addressbook.server;

import com.google.inject.Inject;
import littleware.apps.addressbook.AddressAssetType;
import littleware.asset.server.AssetSpecializerRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi Activator registers addressbook server components with
 * littleware server engine.
 */
public class AddressServerActivator implements BundleActivator {
    @Inject
    public AddressServerActivator( AssetSpecializerRegistry registry_special,
            AddressSpecializer special_address
            ) {
        registry_special.registerService( AddressAssetType.ADDRESS, special_address);
        registry_special.registerService( AddressAssetType.CONTACT, special_address);
    }

    public void start(BundleContext cxt) throws Exception {
    }

    public void stop(BundleContext ctx) throws Exception {
    }

}
