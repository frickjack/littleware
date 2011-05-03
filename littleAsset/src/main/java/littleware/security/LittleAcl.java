/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;


import java.security.acl.Permission;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;

/**
 * Slight specialization of Acl to incorporate into littleware Asset framework.
 * Ignores the p_owner, p_caller arguments, and does not perform Owner checks on methods -
 * assumes Owner is ok.
 * Security check takes place when client tries to save ACL mods back to the
 * Littleware repository.
 * Override Acl methods with no-exception versions.
 */
public interface LittleAcl extends TreeNode {

    public final static String ACL_EVERYBODY_READ = "acl.littleware.everybody.read";
    public final static String ACL_EVERYBODY_WRITE = "acl.littleware.everybody.write";

    /** ACL asset type - with AclManager asset specializer */
    public static final AssetType ACL_TYPE = new AssetType(
            UUIDFactory.parseUUID("04E11B112526462F91152DFFB51D21C9"),
            "littleware.ACL") {

        private final Option<AssetType> superType = Maybe.something( TreeNode.TREE_NODE_TYPE );

        @Override
        public Option<AssetType>  getSuperType() {
            return superType;
        }

        @Override
        public boolean isNameUnique() {
            return true;
        }
    };

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
    public Option<LittleAclEntry> getEntry(LittlePrincipal entry, boolean isNegative);

    @Override
    public Builder copy();


    public interface Builder extends TreeNode.TreeNodeBuilder {

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
        public Builder homeId(UUID value);

        @Override
        public Builder parentId(UUID value);


        @Override
        public Builder createDate(Date value);

        @Override
        public Builder lastUpdateDate(Date value);


        @Override
        public Builder timestamp(long value);

        @Override
        public Builder parent(TreeParent value);

        @Override
        public Builder copy(Asset source);

        @Override
        public LittleAcl build();
    }
}
