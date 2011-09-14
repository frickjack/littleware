/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Binder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AppBootstrap;
import littleware.db.DbGuice;

/**
 * Standalone EntityManagerFactory injection with
 * custom Hibernate bootstrap.  Currently just used
 * for isolated unit tests.
 */
public class HibernateModule extends AbstractGuice {

    private static final Logger log = Logger.getLogger(HibernateModule.class.getName());

    public HibernateModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        log.log(Level.FINE, "Configuring JPA in standalone (hibernate) mode ...");
        try {
            DbGuice.build("littleware_jdbc.properties").configure(binder);
        } catch (IOException ex) {
            throw new AssertionFailedException("Failed to load littleware_jdbc.properties", ex);
        }
        binder.bind(EntityManagerFactory.class).toProvider(HibernateProvider.class);
    }
}