package littleware.security;

import com.google.inject.ImplementedBy;
import littleware.asset.AssetBuilder;

/**
 * Slight extention of Principal interface
 * to support notion of a principal id and comment
 */
public interface LittleGroup extends LittlePrincipal, java.security.acl.Group {	

    @ImplementedBy(GroupBuilder.class)
    public interface Builder extends AssetBuilder {
        public Builder  add( LittlePrincipal principal );
        
        @Override
        public LittleGroup build();
    }
}

