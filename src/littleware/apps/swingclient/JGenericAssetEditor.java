package littleware.apps.swingclient;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;

import littleware.apps.swingclient.event.*;
import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.base.NoSuchThingException;
import littleware.base.Whatever;
import littleware.base.XmlSpecial;
import littleware.base.swing.JUtil;

/** 
 * Simple JPanel based asset editor.
 * Subtypes extend by inserting new components into the core JTabbedPane.
 * Fires a SaveRequestEvent when the user requests to save his edits
 * by pressing the save button.
 */
public abstract class JGenericAssetEditor extends JPanel implements AssetEditor {
	private final static Logger           olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JGenericAssetEditor" );
    protected final AbstractAssetEditor   oeditor_util = new AbstractAssetEditor ( this ) {
        public void eventFromModel ( LittleEvent evt_from_model ) {
            JGenericAssetEditor.this.eventFromModel ( evt_from_model );
        }
    };
    
    
    private final AssetSearchManager    om_search;
    private final AssetManager          om_asset;
    private final IconLibrary           olib_icon;
    private final AssetModelLibrary     olib_asset;
    private final AssetViewFactory      ofactory_view;
    
    private boolean                       ob_changed = false;
    private AssetModel                    omodel_view = null;
    private Asset                         oa_local = null;
    
    private final JAssetLink              owlink_asset;
    private final JTextField              owtext_name = new JTextField ( 60 );
    
    private final JAssetLinkEditor        owalink_to;
    private final JAssetLinkEditor        owalink_from;
    private final JAssetLinkEditor        owalink_acl;
    
    private final JTextArea               owtext_comment = new JTextArea ( 2, 60 );
    private final JTextArea               owtext_update = new JTextArea ( 2, 60 );
    private final JTabbedPane             owtab_stuff = new JTabbedPane ();
    
    private final JButton                 owbutton_save = new JButton ( "Save" );
    private final JButton                 owbutton_reset = new JButton ( "Reset" );

    {
        owbutton_save.setToolTipText ( "Save locally applied changes to the asset repository" );
        owbutton_save.addActionListener ( new ActionListener () {
                public void actionPerformed(ActionEvent event_button) {
                    if ( applyDataFromSummaryUI () ) {
                        oeditor_util.fireLittleEvent ( 
                                                       new SaveRequestEvent (
                                                                             JGenericAssetEditor.this, 
                                                                             getLocalAsset (),
                                                                             getAssetModel ().getLibrary (),
                                                                             owtext_update.getText ()
                                                                             )
                                                       );
                    }
                }
        }										 
                                         );
        owbutton_save.setEnabled( false );
        
        owbutton_reset.setToolTipText ( "Discard all local changes" );
        owbutton_reset.addActionListener ( new ActionListener () {
            public void actionPerformed ( ActionEvent event_button ) {
                clearLocalChanges ();
                updateAssetUI ();
            }
          }
                    );
        owbutton_reset.setEnabled ( false );
    }
    {
        /** Update UI in response to property changes on this object */
        addPropertyChangeListener ( new PropertyChangeListener () {
            public void propertyChange ( PropertyChangeEvent evt_prop ) {
                if ( evt_prop.getPropertyName ().equals ( AssetView.Property.assetModel.toString () ) ) {
                    // Model has changed under us
                    SwingUtilities.invokeLater ( 
                                                 new Runnable () {
                                                     public void run () {
                                                         updateAssetUI ();
                                                     }
                                                 }
                                                 );                
                } else if ( evt_prop.getPropertyName ().equals ( AssetEditor.Property.hasLocalChanges.toString () ) ) {
                    owbutton_save.setEnabled ( (Boolean) evt_prop.getNewValue () );
                    owbutton_reset.setEnabled ( (Boolean) evt_prop.getNewValue () );
                }
            }
        }
                                    );
    }
    
    
    /**
     * Give subtypes access to add into the tab-pain.
     *
     * @param i_index may be -1 to just append to end
     */
    protected void insertTab ( String s_title, Icon icon_title, 
                               Component w_tab, String s_tip,
                               int i_index
                               ) {
        if ( i_index < 0 ) {
            i_index = owtab_stuff.getTabCount ();
        }
        owtab_stuff.insertTab ( s_title, icon_title, w_tab, s_tip, i_index );
    }
    
    /**
     * Let subtypes disable their tab
     *
     * @param w_tab component corresponding to the tab to 
     *                 disable/enable
     * @param b_enable set true to enable, false to disable
     */
    protected void setTabEnabled ( Component w_tab, boolean b_enable ) {
        int i_index = owtab_stuff.indexOfComponent ( w_tab );
        if ( i_index > -1 ) {
            owtab_stuff.setEnabledAt ( i_index, b_enable );
        }
    }
            
    
    
                                    
    /**
     * Build the UI - initialized to a null asset.  
     * Populates the "Generic" editor tab.
     * Subsequent call to setAssetModel() call updateUI()
     * to configure the UI to view a particular AssetModel.
     *
     * @param wpanel_build to build into - usually this
     */
    private void buildAssetUI () {
        GridBagConstraints gcontrol_main = new GridBagConstraints ();
        
        gcontrol_main.gridx = 0;
        gcontrol_main.gridy = 0;
        gcontrol_main.gridwidth = 1;
        gcontrol_main.gridheight = 1;
        
        this.add ( owlink_asset, gcontrol_main );
        gcontrol_main.gridy += gcontrol_main.gridheight;
        gcontrol_main.gridwidth = 4;
        gcontrol_main.gridheight = 5;
        gcontrol_main.fill = GridBagConstraints.BOTH;
        this.add ( owtab_stuff, gcontrol_main );
        
        gcontrol_main.gridy += gcontrol_main.gridheight;        
        gcontrol_main.gridheight = 1;
        gcontrol_main.fill = GridBagConstraints.HORIZONTAL;
        this.add ( new JSeparator (), gcontrol_main );
        gcontrol_main.gridy += gcontrol_main.gridheight;
        this.add ( new JLabel ( "Update note:" ), gcontrol_main );
        
        gcontrol_main.gridy += gcontrol_main.gridheight; 
        gcontrol_main.fill = GridBagConstraints.BOTH;
        gcontrol_main.gridheight = 2;
        this.add ( owtext_update, gcontrol_main );

        gcontrol_main.gridy += gcontrol_main.gridheight; 
        gcontrol_main.fill = GridBagConstraints.NONE;
        gcontrol_main.gridx = 0;
        gcontrol_main.gridwidth = 1;
        gcontrol_main.gridheight = 1;
        this.add ( owbutton_reset, gcontrol_main );
        gcontrol_main.gridx += gcontrol_main.gridwidth;
        this.add ( owbutton_save, gcontrol_main );

        {
            JPanel wpanel_build = new JPanel ();
            wpanel_build.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
            wpanel_build.setLayout ( new GridBagLayout () );
            
            GridBagConstraints gcontrol_summary = new GridBagConstraints();
            gcontrol_summary.gridx = 0;
            gcontrol_summary.gridy = 0;
            gcontrol_summary.gridwidth = 1;
            gcontrol_summary.gridheight = 1;
            
            wpanel_build.add ( new JLabel ( "Name: " ), gcontrol_summary );
            gcontrol_summary.gridx += gcontrol_summary.gridwidth;
            gcontrol_summary.gridwidth = 3;
            gcontrol_summary.fill = GridBagConstraints.HORIZONTAL;
            wpanel_build.add ( owtext_name, gcontrol_summary );
            
            gcontrol_summary.gridx = 0;
            gcontrol_summary.gridwidth = 1;
            gcontrol_summary.fill = GridBagConstraints.NONE;
            gcontrol_summary.gridy += gcontrol_summary.gridheight;
            wpanel_build.add ( new JLabel ( "To: " ), gcontrol_summary );
            gcontrol_summary.gridx += gcontrol_summary.gridwidth;
            gcontrol_summary.gridwidth = 3;
            gcontrol_summary.fill = GridBagConstraints.HORIZONTAL;            
            wpanel_build.add ( owalink_to, gcontrol_summary );
            
            gcontrol_summary.gridx = 0;
            gcontrol_summary.gridwidth = 1;
            gcontrol_summary.fill = GridBagConstraints.NONE;
            gcontrol_summary.gridy += gcontrol_summary.gridheight;
            wpanel_build.add ( new JLabel ( "ACL: " ), gcontrol_summary );
            gcontrol_summary.gridx += gcontrol_summary.gridwidth;
            gcontrol_summary.gridwidth = 3;
            gcontrol_summary.fill = GridBagConstraints.HORIZONTAL;            
            wpanel_build.add ( owalink_acl, gcontrol_summary );
            
            gcontrol_summary.gridx = 0;
            gcontrol_summary.gridwidth = 1;
            gcontrol_summary.fill = GridBagConstraints.NONE;
            gcontrol_summary.gridy += gcontrol_summary.gridheight;
            wpanel_build.add ( new JLabel ( "From: " ), gcontrol_summary );
            gcontrol_summary.gridx += gcontrol_summary.gridwidth;
            gcontrol_summary.fill = GridBagConstraints.HORIZONTAL;            
            gcontrol_summary.gridwidth = 3;
            wpanel_build.add ( owalink_from, gcontrol_summary );
            
            gcontrol_summary.gridx = 0;
            gcontrol_summary.gridwidth = 1;
            gcontrol_summary.gridy += gcontrol_summary.gridheight;
            gcontrol_summary.fill = GridBagConstraints.NONE;            
            wpanel_build.add ( new JLabel ( "Comment: " ), gcontrol_summary );        
            gcontrol_summary.gridx = 0;
            gcontrol_summary.gridy += gcontrol_summary.gridheight;
            gcontrol_summary.gridwidth = 4;
            gcontrol_summary.gridheight = 2;
            gcontrol_summary.fill = GridBagConstraints.BOTH;            
            wpanel_build.add ( owtext_comment, gcontrol_summary );
            
            gcontrol_summary.gridy += gcontrol_summary.gridheight;
            gcontrol_summary.gridheight = 1;
            gcontrol_summary.fill = GridBagConstraints.HORIZONTAL;            
            wpanel_build.add ( new JSeparator (), gcontrol_summary );
            
            // Button to apply GUI summary data to the LOCAL asset (does not do a save)
            final JButton  wbutton_apply = new JButton ( "Apply Changes",
                                                         olib_icon.lookupIcon ( "littleware.apply" )
                                                         );
            wbutton_apply.setToolTipText ( "Apply data to local asset, but do not Save to repository" );
            wbutton_apply.addActionListener ( new ActionListener () {
                public void actionPerformed ( ActionEvent event_button ) {
                    applyDataFromSummaryUI ();
                }
            }
            );

            final JButton  wbutton_sync = new JButton ( "Resync Local" );
            wbutton_sync.setToolTipText ( "Sync basic editor with local asset" );
            wbutton_sync.addActionListener ( new ActionListener () {
                public void actionPerformed ( ActionEvent event_button ) {
                    updateBasicUI ();
                }
            }
            );

            gcontrol_summary.fill = GridBagConstraints.NONE;            
            gcontrol_summary.gridy += gcontrol_summary.gridheight;
            gcontrol_summary.gridx = 0;            
            gcontrol_summary.gridheight = 1;
            gcontrol_summary.gridwidth = 1;
            wpanel_build.add ( wbutton_sync, gcontrol_summary );
            gcontrol_summary.gridx += gcontrol_summary.gridwidth;
            wpanel_build.add ( wbutton_apply, gcontrol_summary );
            
            owtab_stuff.add ( "BasicEdit", wpanel_build );
        }
    }
    

    /**  
     * Internal utility extracts data from the basic editor pane,
     * and attempt to apply the represented changes to the LocalAsset.
     * Should invoke from SwingDispatch thread - popup error dialog on exception.
     *
     * @return true on success applying all changes, false on failure
     */
    public boolean applyDataFromSummaryUI () {
        AssetPathFactory factory_apath = AssetPathFactory.getFactory ();
        try {
            String     s_name = owtext_name.getText ();
            String     s_comment = owtext_comment.getText ();
            UUID       u_to = owalink_to.getLink ();
            UUID       u_from = owalink_from.getLink ();
            UUID       u_acl = owalink_acl.getLink ();

            Asset      a_local = getLocalAsset ();
            
            if ( ! a_local.getName ().equals ( s_name ) ) {
                a_local.setName ( s_name );
                setHasLocalChanges ( true );
            }
            if ( ! a_local.getComment ().equals ( s_comment ) ) {
                a_local.setComment ( s_comment );
                setHasLocalChanges ( true );
            }
            if ( ! Whatever.equalsSafe ( a_local.getToId (), u_to ) ) {
                a_local.setToId ( u_to );
                setHasLocalChanges ( true );
            }
            if ( ! Whatever.equalsSafe ( a_local.getFromId (), u_from ) ) {
                a_local.setFromId ( u_from );
                setHasLocalChanges ( true );
            }
            if ( ! Whatever.equalsSafe ( a_local.getAclId (), u_acl ) ) {
                a_local.setAclId ( u_acl );
                setHasLocalChanges ( true );
            }
            
            return true;
        } catch ( Exception e ) {
            olog_generic.log ( Level.WARNING, "Failed save, caught: " + e );
            JOptionPane.showMessageDialog( null, "Apply failed, caught: " + e, 
                                           "Apply failed", JOptionPane.ERROR_MESSAGE
                                           );
            return false;
        }
    }
    
    /**
     * Public constructor
     * builds the UI, and sets the model to view
     */
    public JGenericAssetEditor (
                                AssetModel   model_view,
                                AssetManager m_asset,
                                AssetSearchManager m_search,
                                IconLibrary lib_icon,
                                AssetViewFactory factory_view
                                ) 
    {
        super ( new GridBagLayout () );
        om_asset = m_asset;
        om_search = m_search;
        olib_icon = lib_icon;   
        olib_asset = model_view.getLibrary ();
        ofactory_view = factory_view;
        owlink_asset = new JAssetLink ( lib_icon );
        
        owalink_to = new JAssetLinkEditor ( m_search, olib_asset,
                                             lib_icon, 
                                             model_view,
                                             factory_view
                                             );
        owalink_from = new JAssetLinkEditor ( m_search, olib_asset,
                                             lib_icon, 
                                             model_view,
                                             factory_view
                                             );
        
        owalink_acl = new JAssetLinkEditor ( m_search, olib_asset,
                                             lib_icon, 
                                             model_view,
                                             factory_view
                                             );
        
        owalink_acl.addLittleListener ( new LittleListener () {
            public void receiveLittleEvent ( LittleEvent event_edit ) {
                if ( event_edit instanceof SelectAssetEvent ) {
                    owalink_acl.setLink ( ((SelectAssetEvent) event_edit).getSelectedAsset () );
                }
            }
        }
                                          );
        
        owalink_to.addLittleListener ( new LittleListener () {
            public void receiveLittleEvent ( LittleEvent event_edit ) {
                if ( event_edit instanceof SelectAssetEvent ) {
                    owalink_to.setLink ( ((SelectAssetEvent) event_edit).getSelectedAsset () );
                }
            }
        }
                                        );

        owalink_from.addLittleListener ( new LittleListener () {
            public void receiveLittleEvent ( LittleEvent event_edit ) {
                if ( event_edit instanceof SelectAssetEvent ) {
                    owalink_from.setLink ( ((SelectAssetEvent) event_edit).getSelectedAsset () );
                }
            }
        }
                                        );
        
        
        buildAssetUI ();
        setAssetModel( model_view );
    }
    
    /**
     * Little internal utility to get an asset-path string
     * to the asset with the given id - handles null
     *
     * @param u_id of asset to get path to or null
     * @param factory_apath to create path with
     * @return AssetPath.toString() if u_id not null, else ""
     */
    private String getPath ( UUID u_id, AssetPathFactory factory_apath ) {
        if ( null == u_id ) {
            return "";
        } else {
            return factory_apath.createPath ( u_id ).toString ();
        }
    }

    /**  
     * Internal utility - resyncs the basic editor pane with the local asset.
     */
    private void updateBasicUI () {
        Asset a_local = getLocalAsset ();
        owlink_asset.setLink ( a_local );
        owtext_name.setText ( a_local.getName () );
        AssetPathFactory factory_apath = AssetPathFactory.getFactory ();
        
        owalink_to.setLink ( a_local.getToId () );
        owalink_from.setLink ( a_local.getFromId () );
        owalink_acl.setLink ( a_local.getAclId () );
        
        owalink_to.setFallbackAsset ( getAssetModel () );
        owalink_from.setFallbackAsset ( getAssetModel () );
        owalink_acl.setFallbackAsset ( getAssetModel () );
        
        owtext_comment.setText ( a_local.getComment () );        
    }        

    /**
     * Reset the entire UI to match the data in getLocalAsset().
     * Subtypes should extend to update the panes they have
     * added via addEditorPane(...).
     */
    protected void updateAssetUI () {
        updateBasicUI ();
    }        
 
    public AssetModel getAssetModel () {
        return oeditor_util.getAssetModel ();
    }
    

    public void setAssetModel ( AssetModel model_view ) {
        oeditor_util.setAssetModel ( model_view );
    }
    
    
    
    public Asset getLocalAsset () {
        return oeditor_util.getLocalAsset ();
    }
    
    
    public void setHasLocalChanges ( boolean b_changed ) {
        oeditor_util.setHasLocalChanges ( b_changed );
    }
    
    public void clearLocalChanges () {
        oeditor_util.clearLocalChanges ();
    }
    
    public boolean getHasLocalChanges () {
        return oeditor_util.getHasLocalChanges ();
    }
    
    public void saveLocalChanges ( AssetManager m_asset, String s_message 
                                   ) throws BaseException, AssetException, 
        RemoteException, GeneralSecurityException
    {
        oeditor_util.saveLocalChanges ( m_asset, s_message );
    }
    
    
	public void	addUndoableEditListener( UndoableEditListener listen_edit ) {
        oeditor_util.addUndoableEditListener ( listen_edit );
    }
	
	public void     removeUndoableEditListener( UndoableEditListener listen_edit ) {
        oeditor_util.removeUndoableEditListener ( listen_edit );
    }
    
    /**
     * Trigger a UI sync call to updateAssetUI
     * if the LittleEvent comes from
     * the getAssetModel() AssetModel (data model update).
     */
    protected void eventFromModel ( LittleEvent evt_prop ) {
        if ( evt_prop.getSource () == getAssetModel () ) {
            // Model has changed under us
            SwingUtilities.invokeLater ( 
                                         new Runnable () {
                                             public void run () {
                                                 updateAssetUI ();
                                             }
                                         }
                                         );                
        }
    }

    /**
     * Give subtypes access to SimpleLittleTool for
     * firing PropertyChangeEvents and LittleEvents to
     * registered listeners
     *
     * @return SimpleLittleTool managing listeners.
     */
    protected SimpleLittleTool getEventTool () {
        return oeditor_util;
    }
    
    
    public void	addLittleListener( LittleListener listen_little ) {
		oeditor_util.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		oeditor_util.removeLittleListener ( listen_little );
	}
    
    
    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        oeditor_util.addPropertyChangeListener ( listen_props );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        oeditor_util.removePropertyChangeListener ( listen_props );
    }
    
    public Asset changeLocalAsset () {
        return oeditor_util.changeLocalAsset ();
    }
	
}    


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

