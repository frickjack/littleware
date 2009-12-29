/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import com.google.inject.ImplementedBy;

import java.security.acl.Permission;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.base.Maybe;

/**
 * Slight specialization of Acl to incorporate into littleware Asset framework.
 * Ignores the p_owner, p_caller arguments, and does not perform Owner checks on methods -
 * assumes Owner is ok.
 * Security check takes place when client tries to save ACL mods back to the
 * Littleware repository.
 * Override Acl methods with no-exception versions.
 */
public interface LittleAcl extends Asset {

    public final static String ACL_EVERYBODY_READ = "acl.littleware.everybody.read";

    /**
     * Get enumeration view of the ACL entries.
     */
    public Enumeration<LittleAclEntry> entries();

    public Collection<LittleAclEntry> getEntries();
    /**
     * Get the permissions associated with the given principal
     */
    public Collection<Permission> getPermissions(LittlePrincipal principal);
    public boolean checkPermission(LittlePrincipal user, Permission permission);

    /**
     * Little utility - get the entry associated with the given Principal,
     * or return null if no entry registered.
     *
     * @param entry Principal we want to get the entry for
     * @param isNegative do we want the postive or negative entry ?
     * @return entry's entry or null if p_entry entry not in this Acl
     */
    public Maybe<LittleAclEntry> getEntry(LittlePrincipal entry, boolean isNegative);

    @Override
    public Builder copy();

    @ImplementedBy(SimpleACLBuilder.class)
    public interface Builder extends AssetBuilder {

        /**
         * Utility since our Acl implementation does not care who the caller is
         */
        public Builder addEntry(LittleAclEntry entry);

        public Builder removeEntry(LittleAclEntry entry);

        @Override
        public Builder name(String value);

        @Override
        public Builder creatorId(UUID value);

        @Override
        public Builder lastUpdaterId(UUID value);

        @Override
        public Builder aclId(UUID value);

        @Override
        public Builder ownerId(UUID value);

        @Override
        public Builder comment(String value);

        @Override
        public Builder lastUpdate(String value);

        @Override
        public Builder data(String value);

        @Override
        public Builder homeId(UUID value);

        @Override
        public Builder fromId(UUID value);

        @Override
        public Builder toId(UUID value);

        @Override
        public Builder startDate(Date value);

        @Override
        public Builder endDate(Date value);

        @Override
        public Builder createDate(Date value);

        @Override
        public Builder lastUpdateDate(Date value);

        @Override
        public Builder value(float value);

        @Override
        public Builder state(int value);

        @Override
        public Builder transaction(long value);

        @Override
        public Builder copy(Asset source);

        @Override
        public LittleAcl build();
    }
}
