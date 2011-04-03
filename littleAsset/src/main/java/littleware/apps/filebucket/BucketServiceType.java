/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.filebucket;


import littleware.apps.filebucket.client.BucketManagerService;
import littleware.base.UUIDFactory;
import littleware.security.auth.ServiceType;

/**
 * Just a namespace to put the BUCKET_SERVICE_TYPE into
 */
public class BucketServiceType extends ServiceType {
    public static final ServiceType<BucketManagerService> BUCKET_MANAGER =
		new ServiceType<BucketManagerService> ( UUIDFactory.parseUUID ( "C09675718D7E4ABC8C825D0000CAA0C4" ),
                                         "littleware.BUCKET_MANAGER_SERVICE",
                                         BucketManagerService.class
                                         );
    private static final long serialVersionUID = 721309844532000264L;
}
