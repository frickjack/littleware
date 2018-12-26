package littleware.security;

import littleware.security.internal.SimpleUserTreeBuilder;
import com.google.inject.ImplementedBy;
import littleware.asset.AssetTreeTemplate;

/**
 * Generally want to put new user in standard
 * layout on tree with maybe a user icon or contact children or
 * add to groups, whatever.
 */
@ImplementedBy(SimpleUserTreeBuilder.class)
public interface UserTreeBuilder {
    public UserTreeBuilder user( String value );

    /**
     * Return a template to build user tree -
     * default implementation builds
     *         parent/Users/1stLetter/NewUser
     */
    public AssetTreeTemplate build();
}
