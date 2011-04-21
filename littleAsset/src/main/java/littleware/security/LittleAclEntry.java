package littleware.security;

import java.security.acl.Permission;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;

/**
 * Interface exported by littleware AclEntry Asset.
 * Not intended to be saved/read directly by clients -
 * rather loaded/saved as part of a LittleAcl - which
 * takes care of setting owner, home, from, to, etc.
 * attributes to be consistent with the Acl it belongs to.
 * NOTE: each entry can belong to only one ACL
 */
public interface LittleAclEntry extends Asset {

    /**
     * Covariant return-type: LittlePrincipal
     */
    public LittlePrincipal getPrincipal();

    public UUID getOwningAclId();

    public UUID getPrincipalId();

    public boolean checkPermission(Permission permission);

    public Collection<Permission> getPermissions();

    public Enumeration<Permission> permissions();

    public boolean isNegative();

    @Override
    public Builder copy();

    
    /** ACL_ENTRY asset type  */
    public static final AssetType ACL_ENTRY = new AssetType(
            UUIDFactory.parseUUID("D23EA8B5A55F4283AEF29DFA50C12C54"),
            "littleware.ACL_ENTRY");

    //-----------------------------------------------
    public interface Builder extends AssetBuilder {

        public Builder addPermission(Permission permission);

        public Builder removePermission(Permission permission);

        public void setNegative();

        public Builder negative();

        public void setPrincipal(LittlePrincipal principal);

        public Builder principal(LittlePrincipal principal);

        /**
         * Synonym for parent()
         */
        public Builder acl(LittleAcl acl);

        @Override
        public LittleAclEntry build();

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
        public Builder createDate(Date value);

        @Override
        public Builder lastUpdateDate(Date value);


        @Override
        public Builder timestamp(long value);

        @Override
        public Builder copy(Asset source);


    }
}
