/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import java.util.UUID;

/**
 * Interface to a central security manager for managing
 * littleware java.security.Principals.
 * A generic AccountManager implementation may not restrict
 * read-access to littleware Principal data, but should
 * enforce write-access restrictions.
 */
public interface AccountManager {

    /** group-name of admin group */
    //public static final String LITTLEWARE_ADMIN_GROUP = "group.littleware.administrator";
    /** Admin user name */
    public static final String LITTLEWARE_ADMIN = "littleware.administrator";
    /** group containing everybody */
    //public static final String LITTLEWARE_EVERYBODY_GROUP = "group.littleware.everybody";
    /** Admin user id */
    public static final UUID UUID_ADMIN = littleware.base.UUIDFactory.parseUUID("00000000000000000000000000000000");
    /** Admin group id */
    public static final UUID UUID_ADMIN_GROUP = littleware.base.UUIDFactory.parseUUID("89A1CB79B5944447BED9F38D398A7D12");
    /** Everybody group id */
    public static final UUID UUID_EVERYBODY_GROUP = littleware.base.UUIDFactory.parseUUID("D701C9B3C9B7453299E89A0161DDC242");    
}