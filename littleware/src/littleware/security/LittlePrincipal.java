package littleware.security;

/**
 * Slight extention of Principal interface
 * to support notion of a principal id and comment
 */
public interface LittlePrincipal extends java.security.Principal, littleware.asset.Asset {
    /** Covariant return-type clone */
    public LittlePrincipal clone ();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

