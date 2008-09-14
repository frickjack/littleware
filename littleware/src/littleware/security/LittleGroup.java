package littleware.security;

/**
 * Slight extention of Principal interface
 * to support notion of a principal id and comment
 */
public interface LittleGroup extends LittlePrincipal, java.security.acl.Group {	
    /** Covariant return-type clone */
    public LittleGroup clone ();

    /** Convenience method - removes all the members from a group */
    public void clearMembers ();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

