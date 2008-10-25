package littleware.apps.swingclient;

import java.awt.*;
import javax.swing.*;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.acl.Group;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.base.NoSuchThingException;
import littleware.security.SecurityAssetType;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;

/** 
 * Swing based AssetView for GROUP type assets.
 */
public class JGroupView extends JAssetWithChildrenView {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JGroupView" );
	

    /**
     * Let our base class know about our members
     */
    @Override
    protected Map<Asset,String> getChildInfo () {
        AssetModel model_view = getAssetModel ();
        Map<Asset,String> v_childinfo = new HashMap<Asset,String> ();

        if ( ! model_view.getAsset ().getAssetType ().equals ( SecurityAssetType.GROUP ) ) {
            super.setTabEnabled ( false );
            return v_childinfo;
        }
        super.setTabEnabled ( true );
        LittleGroup group_view = (LittleGroup) model_view.getAsset ();
        
        for ( Enumeration<? extends Principal> enum_members = group_view.members ();
              enum_members.hasMoreElements ();
              ) {
            v_childinfo.put ( (LittlePrincipal) enum_members.nextElement (), null );
        }
        return v_childinfo;
    }

    /**
     * Constructor sets up the UI to view the given group
     *
     * @param model_group to view
     * @param m_retriever to retrieve asset details with
     * @param lib_icon icon source
     * @exception IllegalArgumentException if model_asset does not reference a group
     */
    public JGroupView( AssetModel model_group, AssetRetriever m_retriever,
                       IconLibrary lib_icon 
                       ) {
        super( model_group, m_retriever, lib_icon, "Group members",
               "Group Members", lib_icon.lookupIcon ( SecurityAssetType.GROUP ),
               "View members of group"
               );
        updateAssetUI ();
    }
        
}