/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Binder;
import javax.persistence.EntityManagerFactory;

/**
 * Standalone EntityManagerFactory injection with
 * custom Hibernate bootstrap.  Currently just used
 * for isolated unit tests.
 */
public class HibernateGuice extends AbstractGuice {


    @Override
    public void configure(Binder binder) {
        super.configure( binder );
        binder.bind(EntityManagerFactory.class).toProvider( HibernateProvider.class );
    }
}