package littleware.security;

import com.google.inject.ImplementedBy;
import java.util.Collection;
import littleware.asset.AssetBuilder;

/**
 * Slight extention of Principal interface
 * to support notion of a principal id and comment
 */
public interface LittleGroup extends LittlePrincipal, java.security.acl.Group {	

    @Override
    public Builder copy();
    
    @ImplementedBy(GroupBuilder.class)
    public interface Builder extends AssetBuilder {
        public Builder  add( LittlePrincipal principal );
        public Builder  addAll( Collection<? extends LittlePrincipal> principalSet );
        @Override
        public LittleGroup build();
    }
}

