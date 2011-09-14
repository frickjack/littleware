/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Binder;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import littleware.bootstrap.AppBootstrap;

/**
 * EntityManager Guice injection when run within a J2EE container
 * that manages our PersistenceManager setup for us.
 */
public class J2EEModule extends AbstractGuice {
    public J2EEModule( AppBootstrap.AppProfile profile ) {
        super( profile );
    }
    
    @Override
    public void configure(Binder binder) {
        super.configure( binder );
        binder.bind(EntityManagerFactory.class).toInstance(
                Persistence.createEntityManagerFactory("littlewarePU"));
    }
}
