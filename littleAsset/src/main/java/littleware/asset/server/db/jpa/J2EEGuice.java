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
import com.google.inject.Module;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * EntityManager Guice injection when run within a J2EE container
 * that manages our PersistenceManager setup for us.
 */
public class J2EEGuice extends AbstractGuice {

    @Override
    public void configure(Binder binder) {
        super.configure( binder );
        binder.bind(EntityManagerFactory.class).toInstance(
                Persistence.createEntityManagerFactory("littlewarePU"));
    }
}
