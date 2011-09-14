/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.jpa;

import javax.persistence.EntityManager;
import littleware.asset.server.LittleTransaction;

/**
 * Specialization of LittleTransaction gives access to a
 * transaction-managed entity manager.
 */
public interface JpaLittleTransaction extends LittleTransaction {
    public EntityManager getEntityManager ();

}
