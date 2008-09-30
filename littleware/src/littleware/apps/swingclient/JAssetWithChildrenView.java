package littleware.apps.swingclient;

import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.acl.*;
import javax.swing.*;
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
 * Abstract specialization of JGenericAssetView that
 * displays a list of child assets in the RightPanel window.
 * Subtypes just need to define the getChildInfo() method.
 */
public abstract class JAssetWithChildrenView extends JGenericAssetView {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JAssetWithChildrenView" );
	
    private final IconLibrary                    olib_icon;
    private final JPanel                         owlist_members;
    private final AssetRetriever                 om_retriever;
    
    // Should replace with a custom list-model later to avoid so many updates
    private final SimpleListModel<Asset>         omodel_children = new SimpleListModel ( new ArrayList<Asset> () );
        
    
    /**
     * Subtypes override with call that returns a map from
     * the children of this asset to include in the view
     * each mapped to an info string to include with
     * the child.  The info string may be empty.
     */
    protected abstract Map<Asset,String> getChildInfo ();
    
    
    private Map<Asset,String>  ov_childinfo = new HashMap<Asset,String> ();
    
    
    @Override
    protected void updateAssetUI () {
        super.updateAssetUI ();

        java.util.List<Asset>        v_members = omodel_children.getList ();
        v_members.clear ();
        
        // Save the child-info for access by setLink before changing the omodel_list
        ov_childinfo = getChildInfo ();
        
        v_members.addAll ( ov_childinfo.keySet () );
        olog_generic.log ( Level.FINE, "Updating child info for: " + getAssetModel ().getAsset ().getName () +
                           ", num children: " + v_members.size ()
                           );

        Collections.sort ( v_members, 
                           new Comparator<Asset> () {
                               public int compare ( Asset a_1, Asset a_2 ) {
                                   return a_1.getName ().compareTo ( a_2.getName () );
                               }
                           }
                           );
        
        omodel_children.fireChangeEvent ();
        /*... old DefaultListModel way ...
        omodel_children.clear ();
        for ( Asset a_child : v_members ) {
            omodel_children.addElement ( a_child );
        }        
        ...*/
    }
    
    /**
     * Let subtypes disable the generic-child tab
     *
     * @param b_enable set true to enable, false to disable
     */
    protected void setTabEnabled ( boolean b_enable ) {
        setTabEnabled ( owlist_members, b_enable );
    }
    
    
    /**
     * Constructor sets up the UI to view the given group.
     * Subtype constructor should invoke updateChildren() to populate the
     * child-info list.
     *
     * @param model_with_children to view
     * @param m_retriever to retrieve asset details with
     * @param lib_icon icon source
     * @param s_children_header label for the children-list area (ex: "Group members")
     * @param s_tab_label short string to label the child-tab with
     * @param icon_tab icon to label the child-tab with - may be null
     * @exception IllegalArgumentException if model_asset does not reference a group
     */
    protected JAssetWithChildrenView( AssetModel model_with_children, AssetRetriever m_retriever,
                                      IconLibrary lib_icon, String s_children_header,
                                      String s_tab_label, Icon icon_tab, String s_tab_tooltip
                       ) {
        super( model_with_children, m_retriever, lib_icon );
        om_retriever = m_retriever;
        olib_icon = lib_icon;
        owlist_members = new JAssetLinkList ( omodel_children,
                                              lib_icon,
                                              model_with_children.getLibrary (),
                                              m_retriever,
                                              s_children_header
                                              ) {
            /**
             * Customize setLink to append asset-specific info from
             * getChildInfo() if available.
             */
            protected void setLink ( JAssetLink wlink_asset, Asset a_view ) {
                wlink_asset.setLink ( a_view );
                String s_value = ov_childinfo.get ( a_view );
                if ( null != s_value ) {
                    wlink_asset.setText ( wlink_asset.getText () + ": " + s_value );
                }
            }
                        
        };
        
        ((JAssetLinkList) owlist_members).addLittleListener (
                                      olisten_bridge
                                      );
        
        insertTab ( s_tab_label, icon_tab,
                    owlist_members, s_tab_tooltip,
                    -1
                    );        
    }
    
}