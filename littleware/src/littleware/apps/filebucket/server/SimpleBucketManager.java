package littleware.apps.filebucket.server;

import java.io.*;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.filebucket.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.asset.*;
import littleware.asset.server.TransactionManager;
import littleware.asset.server.AssetResourceBundle;
import littleware.base.PropertiesLoader;


/**
 * Simple implementation of BucketManager interface.
 * Just stuffs all the assets under a set of roots -
 * select a root for an asset by hasing on the asset id first,
 * but check all roots if bucket not found there.
 */
public class SimpleBucketManager implements BucketManager {
    private static final Logger   olog_generic = Logger.getLogger ( SimpleBucketManager.class.getName () );
    
    private static  String   os_root_root = "";
    static {
        try {
            os_root_root = PropertiesLoader.get ().loadProperties( ).getProperty( "littleware.bucket.root" );
        } catch ( IOException e ) {
            olog_generic.log( Level.SEVERE, "Caught unexpected loading littleware.bucket.root property from littleware.properties", e );
            os_root_root = "";
        }
    }
    private final static String[] ov_roots = {
        os_root_root + "/Library/LittlewareAssets/Volume1",
        os_root_root + "/Library/LittlewareAssets/Volume2",
        os_root_root + "/Library/LittlewareAssets/Volume3",
        os_root_root + "/Library/LittlewareAssets/Volume4",
        os_root_root + "/Library/LittlewareAssets/Volume5",
        os_root_root + "/Library/LittlewareAssets/Volume6",
        os_root_root + "/Library/LittlewareAssets/Volume7",
        os_root_root + "/Library/LittlewareAssets/Volume8",
        os_root_root + "/Library/LittlewareAssets/Volume9"
    };
    
    private final AssetSearchManager      om_search;
    private final AssetManager            om_asset;
    
    
    /** Constructor pulls AssetSearchManager out of the asset ResourceBundle 
    public SimpleBucketManager () { 
        AssetResourceBundle bundle_asset = AssetResourceBundle.getBundle ();
        
        om_search = (AssetSearchManager) bundle_asset.getObject ( AssetResourceBundle.Content.AssetSearcher );
        om_asset = (AssetManager) bundle_asset.getObject ( AssetResourceBundle.Content.AssetManager );
    }
    */
    
    /** 
     * Contructor takes user-suppled AssetSearchManager
     *
     * @param m_search to lookup asset info with
     * @param m_asset to update asset when data added to bucket
     */
    public SimpleBucketManager ( AssetSearchManager m_search, AssetManager m_asset ) {
        om_search = m_search;
        om_asset = m_asset;
    }
    
    
    /** 
     * Simple first-pass system for assigning a bucket-path to an asset.
     * 
     * @param a_in Asset that needs a new bucket
     * @return File specifying a directory which may be used as the asset&apos;s bucket -
     *            the directory may not yet exist
     */
    public  File  getBucketPath ( Asset a_in ) {
        String s_default_root = ov_roots[ Math.abs( a_in.getObjectId ().hashCode () % ov_roots.length ) ];
        File   file_root = new File ( s_default_root );
        
        if ( ! file_root.exists () ) { // check the other roots just 2b safe
            File  file_check = null;
            for ( String s_root : ov_roots ) {
                file_check = new File( s_root );
                if ( file_check.exists () ) {
                    file_root = file_check;
                    break;
                }
            }
        }
        return new File ( file_root, UUIDFactory.makeCleanString ( a_in.getObjectId () ) + "/userdata" );
    }
                
              

    
    public Bucket getBucket ( UUID u_asset ) throws BaseException, GeneralSecurityException,
                                AssetException, RemoteException, BucketException, IOException
    {
        Asset        a_bucket = om_search.getAsset ( u_asset );
        File         file_bucket = getBucketPath ( a_bucket );
        SortedSet<String> v_members = new TreeSet<String> ();
        
        if ( file_bucket.exists () ) {
            Collections.addAll ( v_members, file_bucket.list () );
        }
        return new SimpleBucket ( a_bucket.getObjectId (), v_members );
    }
    
    

    public Asset writeToBucket ( Asset a_in, String s_path, 
                                  String s_data, String s_update_comment 
                                  ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        return writeToBucket ( a_in, s_path, s_data.getBytes ( "UTF-8" ), s_update_comment );
    }
    
    /**
     * Little internal utility - make sure a path does not contain 
     * illegal characters.
     *
     * @param s_path into bucket to check
     * @exception IllegalBucketPathException if path is illegal
     */
    public void checkBucketPath ( String s_path ) throws IllegalBucketPathException {
        if ( (null == s_path)
             || (0 <= s_path.indexOf ( '/' ))
             || (0 <= s_path.indexOf ( '\\' ))
             || s_path.startsWith ( "." )
             ) {
            throw new IllegalBucketPathException ( "Illegal path: " + s_path );
        }
    }        

    public Asset writeToBucket ( Asset a_bucket, String s_path, 
                                byte[] v_data, String s_update_comment
                                ) throws BaseException, GeneralSecurityException,
        AssetException, IOException, RemoteException, BucketException
    {
        checkBucketPath ( s_path );
        Map<UUID,Asset> v_cache = TransactionManager.getTheThreadTransaction ().startDbAccess ();
        try {
            // increment transaction count before writing anything - verify write permission
            olog_generic.log ( Level.FINE, "Writing to bucket " + a_bucket + ", path: " + s_path );
            a_bucket.save ( om_asset, s_update_comment );

            File              file_parent = getBucketPath ( a_bucket );
            
            if ( ! file_parent.exists () ) {
                file_parent.mkdirs ();
            }
            
            File              file_data = new File ( file_parent, s_path );
            FileOutputStream  streamout_data = new FileOutputStream ( file_data );
            
            try {
                streamout_data.write ( v_data );
            } finally {
                streamout_data.close ();
            }
            return a_bucket;
        } finally {
            TransactionManager.getTheThreadTransaction ().endDbAccess ( v_cache );
        }
    }
    

    public String readTextFromBucket ( UUID u_asset, String s_path 
                                       ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        return new String ( readBytesFromBucket ( u_asset, s_path ), "UTF-8" );
    }
    

    public byte[] readBytesFromBucket ( UUID u_asset, String s_path 
                                        ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        checkBucketPath ( s_path );
        Map<UUID,Asset> v_cache = TransactionManager.getTheThreadTransaction ().startDbAccess ();
        try {
            Asset             a_bucket = om_search.getAsset ( u_asset );
            File              file_data = new File ( getBucketPath ( a_bucket ), s_path );
            FileInputStream  streamin_data = new FileInputStream ( file_data );
            try {
                byte[]         v_data = new byte[ streamin_data.available () ];
                streamin_data.read ( v_data );
                return v_data;
            } finally {
                streamin_data.close ();
            }
        } finally {
            TransactionManager.getTheThreadTransaction ().endDbAccess ( v_cache );
        }        
    }
    

    public Asset eraseFromBucket ( Asset a_bucket, String s_path,
                                   String s_update_comment
                                    ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        checkBucketPath ( s_path );
        Map<UUID,Asset> v_cache = TransactionManager.getTheThreadTransaction ().startDbAccess ();
        try {
            File              file_data = new File ( getBucketPath ( a_bucket ), s_path );
            
            if ( ! file_data.exists () ) {
                return a_bucket;
            }
            a_bucket.save ( om_asset, s_update_comment );
            file_data.delete ();
            return a_bucket;
        } finally {
            TransactionManager.getTheThreadTransaction ().endDbAccess ( v_cache );
        }        
    }
    

    public Asset renameFile ( Asset a_bucket, String s_start_path, String s_rename_path,
                               String s_update_comment
                               ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        checkBucketPath ( s_start_path );
        checkBucketPath ( s_rename_path );
        Map<UUID,Asset> v_cache = TransactionManager.getTheThreadTransaction ().startDbAccess ();
        try {
            File              file_data = new File ( getBucketPath ( a_bucket ), s_start_path );
            File              file_rename = new File ( getBucketPath ( a_bucket ), s_rename_path );
            if ( ! file_data.exists () ) {
                return a_bucket;
            }
            // verify write permission before doing the rename
            a_bucket.save ( om_asset, s_update_comment );
            file_data.renameTo ( file_rename );
            return a_bucket;
        } finally {
            TransactionManager.getTheThreadTransaction ().endDbAccess ( v_cache );
        }                
    }
    

    public Asset copyFile ( UUID u_in, String s_in_path, 
                            Asset a_out, String s_copy_path,
                            String s_update_comment
                             ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        Map<UUID,Asset> v_cache = TransactionManager.getTheThreadTransaction ().startDbAccess ();
        try {
            // Do a read, then a write
            byte[] v_data = readBytesFromBucket ( u_in, s_in_path );
            return writeToBucket ( a_out, s_copy_path, v_data, s_update_comment );
        } finally {
            TransactionManager.getTheThreadTransaction ().endDbAccess ( v_cache );
        }                            
    }
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

