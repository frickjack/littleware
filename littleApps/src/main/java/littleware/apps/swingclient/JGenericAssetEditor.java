/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import littleware.base.feedback.SimpleLittleTool;
import littleware.base.feedback.LittleListener;
import littleware.base.feedback.LittleEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.*;

import littleware.apps.client.*;
import littleware.apps.client.event.AssetModelEvent;
import littleware.apps.swingclient.event.*;
import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.base.swing.GridBagWrap;

/** 
 * Simple JPanel based asset editor.
 * Subtypes extend by inserting new components into the core JTabbedPane.
 * Fires a SaveRequestEvent when the user requests to save his edits
 * by pressing the save button.
 */
public class JGenericAssetEditor extends JPanel implements AssetEditor {
	private final static Logger           olog_generic = Logger.getLogger ( JGenericAssetEditor.class.getName() );
    private static final long serialVersionUID = 2345027233175367562L;
    protected final AbstractAssetEditor   oeditor_util = new AbstractAssetEditor ( this ) {
        @Override
        public void eventFromModel ( LittleEvent evt_from_model ) {
            JGenericAssetEditor.this.eventFromModel ( evt_from_model );
        }
    };
    
    
    private final AssetSearchManager    om_search;
    private final AssetManager          om_asset;
    private final IconLibrary           olib_icon;
    private final AssetModelLibrary     olib_asset;
    
    private boolean                       ob_changed = false;
    private AssetModel                    omodel_view = null;
    private Asset                         oa_local = null;
    
    private final JAssetLink              owlink_asset;
    private final JTextField              owtext_name = new JTextField ( 60 );
    private final JTextArea               owtext_comment = new JTextArea ( 2, 60 );
    {
        final FocusListener listener =new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                applyDataFromSummaryUI();
            }

        };

        owtext_name.addFocusListener( listener );
        owtext_comment.addFocusListener( listener );
    }
    
    private final JAssetLinkEditor        owalink_to;
    private final JAssetLinkEditor        owalink_from;
    private final JAssetLinkEditor        owalink_acl;
    private final JAssetLinkEditor        owalink_owner;
    
    private final JTextArea               owtext_update = new JTextArea ( 2, 60 );
    private final JTabbedPane             owtab_stuff = new JTabbedPane ();
    
    private final JButton                 owbutton_save = new JButton ( "Save" );
    private final JButton                 owbutton_reset = new JButton ( "Reset" );

    {
        owbutton_save.setToolTipText ( "Save locally applied changes to the asset repository" );
        owbutton_save.addActionListener ( new ActionListener () {
            @Override
                public void actionPerformed(ActionEvent event_button) {
                    if ( applyDataFromSummaryUI () ) {
                        oeditor_util.fireLittleEvent ( 
                                                       new SaveRequestEvent (
                                                                             JGenericAssetEditor.this, 
                                                                             null,
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
            @Override
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
            @Override
            public void propertyChange ( PropertyChangeEvent evt_prop ) {
                if ( evt_prop.getPropertyName ().equals ( AssetView.Property.assetModel.toString () ) ) {
                    // Model has changed under us
                    SwingUtilities.invokeLater ( 
                                                 new Runnable () {
                        @Override
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
        {
            JPanel wpanel_build = new JPanel ();
            //wpanel_build.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
            wpanel_build.setLayout ( new GridBagLayout () );

            final GridBagWrap gb = GridBagWrap.wrap( wpanel_build );
            for( NameWidget<JComponent> pair: Arrays.asList(
                    new NameWidget<JComponent>( "Name", owtext_name ),
                    new NameWidget<JComponent>( "To", owalink_to ),
                    new NameWidget<JComponent>( "ACL", owalink_acl ),
                    new NameWidget<JComponent>( "Owner", owalink_owner ),
                    new NameWidget<JComponent>( "From", owalink_from )
                    )) {
                gb.add ( new JLabel ( pair.getName() + ": " ) ).nextCol().
                    remainderX().fillX().add( pair.getComp() ).
                    newRow().fillNone().gridwidth(1);
            }

            final JScrollPane wScrollComment = new JScrollPane( owtext_comment );
            wScrollComment.setPreferredSize(
                    new Dimension( 400, 100 )
                    );
            gb.add( new JLabel( "Comment: " ) ).newRow().
                    remainderX().fillBoth().remainderY().
                    add( wScrollComment );
            wpanel_build.setPreferredSize( new Dimension( 500, 400 ) );
            owtab_stuff.add ( "BasicEdit", wpanel_build );
        }
        
        this.add ( owlink_asset, BorderLayout.NORTH );
        this.add ( owtab_stuff, BorderLayout.CENTER );
        final JPanel jPanelButtons = new JPanel ( new GridBagLayout () );
        jPanelButtons.setBorder( BorderFactory.createLineBorder(Color.black) );
        final GridBagWrap  gb = GridBagWrap.wrap( jPanelButtons );
        gb.fillNone().gridwidth( 1 ).gridheight( 1 ).
                add( owbutton_reset ).nextCol().
                add( owbutton_save ).newRow();
        gb.add ( new JLabel ( "Update note:" ) ).nextCol().//newRow().
                fillBoth().remainderX().remainderY().
                add( owtext_update );
        this.add( jPanelButtons, BorderLayout.SOUTH );
    }
    

    /**  
     * Internal utility extracts data from the basic editor pane,
     * and attempt to apply the represented changes to the LocalAsset.
     * Should invoke from SwingDispatch thread - popup error dialog on exception.
     *
     * @return true on success applying all changes, false on failure
     */
    private boolean applyDataFromSummaryUI () {
        try {
            final String     s_name = owtext_name.getText ();
            final String     s_comment = owtext_comment.getText ();
            final AssetBuilder      builder = changeLocalAsset ();
            
            if ( ! builder.getName ().equals ( s_name ) ) {
                builder.setName ( s_name );
            }
            if ( ! builder.getComment ().equals ( s_comment ) ) {
                builder.setComment ( s_comment );
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
    @Inject
    public JGenericAssetEditor (
                                AssetModelLibrary libAsset,
                                AssetManager m_asset,
                                AssetSearchManager m_search,
                                IconLibrary lib_icon,
                                Provider<JAssetLink>  provideLinkView,
                                Provider<JAssetLinkEditor> provideLinkEditor
                                ) 
    {
        super ( new BorderLayout () );
        om_asset = m_asset;
        om_search = m_search;
        olib_icon = lib_icon;   
        olib_asset = libAsset;
        owlink_asset = provideLinkView.get();
        owalink_to = provideLinkEditor.get();
        owalink_from = provideLinkEditor.get();
        owalink_acl = provideLinkEditor.get();
        owalink_owner = provideLinkEditor.get();

        owalink_owner.addLittleListener ( new LittleListener () {
            @Override
            public void receiveLittleEvent ( LittleEvent event_edit ) {
                if ( event_edit instanceof SelectAssetEvent ) {
                    final Asset aSelect = ((SelectAssetEvent) event_edit).getSelectedAsset ();
                    if ( ! Whatever.get().equalsSafe( aSelect.getId(), getLocalAsset().getOwnerId() ) ) {
                        changeLocalAsset().setOwnerId( aSelect.getId() );
                        owalink_owner.setLink ( aSelect.getId() );
                    }
                }
            }
        }
                                        );
        
        owalink_acl.addLittleListener ( new LittleListener () {
            @Override
            public void receiveLittleEvent ( LittleEvent event_edit ) {
                if ( event_edit instanceof SelectAssetEvent ) {
                    final Asset aSelect = ((SelectAssetEvent) event_edit).getSelectedAsset ();
                    if ( ! Whatever.get().equalsSafe(aSelect.getId() , getLocalAsset().getAclId() )) {
                        changeLocalAsset().setAclId( aSelect.getId() );
                        owalink_acl.setLink ( aSelect.getId() );
                    }
                }
            }
        }
                                          );
        
        owalink_to.addLittleListener ( new LittleListener () {
            @Override
            public void receiveLittleEvent ( LittleEvent event_edit ) {
                if ( event_edit instanceof SelectAssetEvent ) {
                    final Asset aSelect = ((SelectAssetEvent) event_edit).getSelectedAsset ();
                    if ( ! Whatever.get().equalsSafe( aSelect.getId() , getLocalAsset().getToId() ) ){
                        changeLocalAsset().setToId( aSelect.getId() );
                        owalink_to.setLink ( aSelect.getId() );
                    }
                }
            }
        }
                                        );

        owalink_from.addLittleListener ( new LittleListener () {
            @Override
            public void receiveLittleEvent ( LittleEvent event_edit ) {
                if ( event_edit instanceof SelectAssetEvent ) {
                    final Asset aSelect = ((SelectAssetEvent) event_edit).getSelectedAsset ();
                    if ( ! Whatever.get().equalsSafe(aSelect.getId(), getLocalAsset().getFromId() )) {
                        changeLocalAsset().setFromId( aSelect.getId() );
                        owalink_from.setLink ( aSelect.getId() );
                    }
                }
            }
        }
                                        );
        
        buildAssetUI ();
    }
    

    /**  
     * Internal utility - resyncs the basic editor pane with the local asset.
     */
    private void updateBasicUI () {
        Asset a_local = getLocalAsset ();
        owlink_asset.setLink ( a_local );
        owtext_name.setText ( a_local.getName () );
        
        owalink_to.setLink ( a_local.getToId () );
        owalink_from.setLink ( a_local.getFromId () );
        owalink_acl.setLink ( a_local.getAclId () );
        owalink_owner.setLink ( a_local.getOwnerId () );
        
        owalink_to.setFallbackAsset ( getAssetModel () );
        owalink_from.setFallbackAsset ( getAssetModel () );
        owalink_acl.setFallbackAsset ( getAssetModel () );
        owalink_owner.setFallbackAsset ( getAssetModel () );
        
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
 
    @Override
    public AssetModel getAssetModel () {
        return oeditor_util.getAssetModel ();
    }
    

    @Override
    public void setAssetModel ( AssetModel model_view ) {
        oeditor_util.setAssetModel ( model_view );
    }
    
    
    
    @Override
    public Asset getLocalAsset () {
        return oeditor_util.getLocalAsset ();
    }
    
    
    @Override
    public void setHasLocalChanges ( boolean b_changed ) {
        oeditor_util.setHasLocalChanges ( b_changed );
    }
    
    @Override
    public void clearLocalChanges () {
        oeditor_util.clearLocalChanges ();
        SwingUtilities.invokeLater ( 
                                     new Runnable () {
            @Override
                                         public void run () {
                                             updateAssetUI ();
                                         }
                                     }
                                     );                 
    }
    
    @Override
    public boolean getHasLocalChanges () {
        return oeditor_util.getHasLocalChanges ();
    }
    
    @Override
    public void saveLocalChanges ( AssetManager m_asset, String s_message 
                                   ) throws BaseException, AssetException, 
        RemoteException, GeneralSecurityException
    {
        oeditor_util.saveLocalChanges ( m_asset, s_message );
    }
    
    
    
    /**
     * Trigger a UI sync call to updateAssetUI
     * if the LittleEvent comes from
     * the getAssetModel() AssetModel (data model update).
     */
    protected void eventFromModel ( LittleEvent evt_in ) {
        if ( evt_in.getSource () == getAssetModel () ) {
            if ( evt_in instanceof AssetModelEvent ) {
                AssetModelEvent evt_asset = (AssetModelEvent) evt_in;
                if ( evt_asset.getOp().equals ( AssetModel.Operation.assetUpdated ) ) {
                    clearLocalChanges ();
                }
            }
            // Model has changed under us
            SwingUtilities.invokeLater ( 
                                         new Runnable () {
                @Override
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
    
    
    @Override
    public void	addLittleListener( LittleListener listen_little ) {
		oeditor_util.addLittleListener ( listen_little );
	}
	
	
    @Override
	public void     removeLittleListener( LittleListener listen_little ) {
		oeditor_util.removeLittleListener ( listen_little );
	}
    
    @Override
    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        oeditor_util.addPropertyChangeListener ( listen_props );
    }
    
    @Override
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        oeditor_util.removePropertyChangeListener ( listen_props );
    }
    
    @Override
    public AssetBuilder changeLocalAsset () {
        return oeditor_util.changeLocalAsset ();
    }

    @Override
    public Feedback getFeedback() {
        return oeditor_util.getFeedback();
    }

    @Override
    public void setFeedback( Feedback feedback ) {
        oeditor_util.setFeedback( feedback );
    }

}    

