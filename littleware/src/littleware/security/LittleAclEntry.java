package littleware.security;

import com.google.inject.ImplementedBy;
import java.security.Principal;
import java.security.acl.AclEntry;

import java.security.acl.Permission;
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
public interface LittleAclEntry extends AclEntry, Asset {

    /**
     * Covariant return-type: LittlePrincipal
     */
    @Override
    public LittlePrincipal getPrincipal ();
    @Override
    public Builder copy();

    @ImplementedBy(AclEntryBuilder.class)
    public interface Builder extends AssetBuilder {
        public Builder addPermission(Permission permission);
        public Builder removePermission( Permission permission );

        public void setNegative();
        public Builder negative();

        public void setPrincipal( LittlePrincipal principal );
        public Builder principal ( LittlePrincipal principal );

        @Override
        public LittleAclEntry build();
    }
    
}


