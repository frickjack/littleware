package littleware.security;

import java.io.Serializable;
import java.security.Principal;



/**
 * Stole this from the jakarta-slide project
 */
public class SimpleRole implements LittleRole {
    
    private final String os_name;
    
    public SimpleRole(String s_name) {
        os_name = s_name;
    }
    
    public String getName() {
        return os_name;
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
    
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean equals( Object x_other ) {
        if ( ! (x_other instanceof SimpleRole) ) {
            return false;
        }
        final SimpleRole role_other = (SimpleRole) x_other;
        return role_other.getName().equals(getName());
    }
}    
