package littleware.security;

import com.google.inject.ImplementedBy;

import java.security.acl.Permission;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;


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

    public boolean checkPermission(Permission permission);

    public Collection<Permission> getPermissions();

    public Enumeration<Permission> permissions();

    public boolean isNegative();

    @Override
    public Builder copy();

    @ImplementedBy(AclEntryBuilder.class)
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
    }
}


