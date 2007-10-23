package littleware.apps.swingclient;

import java.awt.*;
import javax.swing.*;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.acl.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.base.NoSuchThingException;
import littleware.security.*;


/** 
 * Swing based AssetView for ACL type assets.
 */
public class JAclView extends JAssetWithChildrenView {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JAclView" );
	
    
    /**
     * Let our base class know about our members
     */
    @Override
    protected Map<Asset,String> getChildInfo () {
        AssetModel model_view = getAssetModel ();
        Map<Asset,String> v_childinfo = new HashMap<Asset,String> ();
        
        if ( ! model_view.getAsset ().getAssetType ().equals ( SecurityAssetType.ACL ) ) {
            olog_generic.log ( Level.FINE, "Ignoring non-ACL asset" );
            super.setTabEnabled ( false );
            return v_childinfo;
        }
        setTabEnabled ( true );
        
        LittleAcl acl_view = (LittleAcl) model_view.getAsset ();

        StringBuilder    sb_perms = new StringBuilder( 128 );
        for ( Enumeration<AclEntry> enum_members = acl_view.entries ();
              enum_members.hasMoreElements ();
              ) {
            LittleAclEntry  acl_entry = (LittleAclEntry) enum_members.nextElement ();
            LittlePrincipal p_entry = acl_entry.getPrincipal ();

            sb_perms.setLength ( 0 );
            for ( Enumeration<Permission> enum_perm = acl_entry.permissions (); 
                  enum_perm.hasMoreElements ();
                  ) {
                sb_perms.append ( enum_perm.nextElement ().toString () );
            }
            String s_info = sb_perms.toString ();
            olog_generic.log ( Level.FINE, "Adding to child info: " + s_info );
            v_childinfo.put ( p_entry, s_info );
        }
        return v_childinfo;
    }
    
    /**
     * Constructor sets up the UI to view the given acl
     *
     * @param model_acl to view
     * @param m_retriever to retrieve asset details with
     * @param lib_icon icon source
     * @exception IllegalArgumentException if model_asset does not reference a group
     */
    public JAclView( AssetModel model_acl, AssetRetriever m_retriever,
                       IconLibrary lib_icon 
                       ) {
        super( model_acl, m_retriever, lib_icon, "ACL entries",
               "ACL Entries", lib_icon.lookupIcon( SecurityAssetType.ACL ),
               "View ACL entries"
               );
        updateAssetUI ();
    }
    
}