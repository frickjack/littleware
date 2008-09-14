package littleware.asset;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.BaseException;
import littleware.base.ParseException;
import littleware.base.AssertionFailedException;

/**
 * Convenience baseclass for AssetPath implementations 
 */
public abstract class AbstractAssetPath implements AssetPath {
    private static Logger  olog_generic = Logger.getLogger ( "littleware.asset.AbstractAssetPath" );
    private String os_subroot_path = null;
    private String os_path = null;
    
    /** 
     * Do-nothing constructor required for java.io.Serializable
     */
    protected AbstractAssetPath () {}
    
    /**
     * Constructor stashes the string path after processing
     * it through AssetFactory.cleanupPath
     *
     * @param s_path of form /ROOT/A/B/C or whatever
     */
    protected AbstractAssetPath ( String s_path ) {
        os_path = AssetPathFactory.cleanupPath ( s_path );
        int i_slash = os_path.indexOf( "/", 1 );
        if ( i_slash < 0 ) {
            os_subroot_path = "";
        } else {
            os_subroot_path = os_path.substring ( i_slash );
        }
    }
    
    public boolean hasRootBacktrack () {
        return os_subroot_path.startsWith ( "/.." );
    }
    

    public AssetPath normalizePath ( AssetSearchManager m_search
                                     ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        String s_normal = os_subroot_path;
        if ( s_normal.startsWith ( ".." ) ) { // just in case
            s_normal = "/" + s_normal;
        }
        if ( ! s_normal.startsWith ( "/.." ) ) {
            return this;
        }
        Asset a_root = getRoot( m_search );
        for ( ;
              s_normal.startsWith ( "/.." );
              s_normal = s_normal.substring ( 3 )
              ) {
            if ( null == a_root.getFromId () ) {
                throw new DanglingLinkException ( "Unable to normalize path for " + this );
            }
            a_root = m_search.getAsset ( a_root.getFromId () );
        }
        return AssetPathFactory.getFactory ().createPath ( a_root.getObjectId (), s_normal );
    }
    
    /** Subtype needs to override */
    public abstract Asset getRoot ( AssetSearchManager m_search 
                           ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException;
    
    public String getSubRootPath () {
        return os_subroot_path;
    }
    
    public Asset getAsset ( AssetSearchManager m_search 
                            ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        return getAsset ( m_search, true );
    }
    
    public Asset getAsset ( AssetSearchManager m_search,
                            boolean b_resolve_link
                            ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        return m_search.getAssetAtPath ( this );
    }
    
    
        
    public boolean hasParent () {
        return os_subroot_path.matches ( "^(/\\.\\.)*/.+$" );
    }
    
    /**
     * Relies on clone to assemble the parent path if hasParent(),
     * otherwise just returns this.
     */
    public AssetPath getParent () {
        if ( hasParent () ) {
            try {
                AbstractAssetPath path_result = (AbstractAssetPath) this.clone ();
                path_result.os_subroot_path = os_subroot_path.substring( 0, 
                                                                                     os_subroot_path.lastIndexOf( "/" )
                                                                                     );
                path_result.os_path = os_path.substring( 0, 
                                                         os_path.lastIndexOf( "/" )
                                                         );
                return path_result;
            } catch ( StringIndexOutOfBoundsException e ) {
                olog_generic.log ( Level.INFO, "Unexpected " + e + " processing " + os_path +
                                   ", " + os_subroot_path
                                   );
                throw e;
            }
        } else {
            return this;
        }
    }

    
    /** Just return constructor-supplied path string */ 
    public String toString () { return os_path; }

    /** Just hash on toString() */
    public int hashCode () { 
        return os_path.hashCode ();
    }

    /** Just compare on toString () */
    public int compareTo ( AssetPath path_other ) {
        return os_path.compareTo ( path_other.toString () );
    }

    /** Just call through to super - subclass also needs to implement */
    public AbstractAssetPath clone () {
        try {
            return (AbstractAssetPath) super.clone ();
        } catch ( CloneNotSupportedException e ) {
            throw new AssertionFailedException ( "Clone should be supported here", e );
        }
    }

    @Override
    public boolean equals ( Object x_other ) {
        return (
                (x_other instanceof AbstractAssetPath)
                && x_other.toString ().equals ( this.toString () )
                );
    }
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
