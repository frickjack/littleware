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

import com.google.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.event.ActionEvent;
import javax.swing.*;

import littleware.apps.client.*;
import littleware.apps.swingclient.event.*;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;


/**
 * Specialization of JToolbar that prepopulates the
 * toolbar with a set of buttons associated with a supplied view
 * to fascilitate wiring up standard controls.
 * Includes a GOTO-path text-field, so setFloatable(false) is set by constructor.
 */
public class JSimpleAssetToolbar extends JToolBar implements PropertyChangeListener, LittleTool {
    private final static Logger         olog_generic = Logger.getLogger ( JSimpleAssetToolbar.class.getName() );
    private static final long serialVersionUID = 3427883110446003286L;


    private  AssetView             oview_component = null;
    private final AssetModelLibrary     olib_asset;
    private final IconLibrary           olib_icon;
    private final AssetSearchManager    om_search;
    private final SimpleLittleTool      otool_handler = new SimpleLittleTool ( this );
    private final AssetPathFactory      ofactoryPath;

    private final List<UUID>            olist_navigation = new ArrayList<UUID> ();
    private int                         oi_nav_position = 0;
    private static final int            OI_HISTORY_LIMIT = 20;
    
    /**
     * Enumeration of buttons automatically setup in the toolbar.
     * Pass to getButton() to retrieve buttons to listen to.
     * <ul>
     * <li> BACK, FORWARD -  throws NavRequestEvent to listeners when button pressed,
     *        and updates button state based on NavRequestEvents fired
     *        by the LittleTool this guy is listening to. </li>
     * <li> EDIT - throws EditRequestEvent to listeners </li>
     * <li> CREATE - throws CreateRequestEvent to listeners </li>
     * </ul>
     */
    public enum ButtonId { 
        BACK,
        FORWARD,
        UP,
        REFRESH,
        CREATE,
        EDIT,
        DELETE,
        GOTO
    }
    
    private final Map<ButtonId,JButton>   omap_button = new EnumMap<ButtonId,JButton> ( ButtonId.class );
    private final JTextField              owtext_goto_path = new JTextField ( 40 );
    private       UUID                    ou_goto = null;
    
    /**
     * Internal utility to setup buttons in this toolbar.
     * Initializes all the buttons, only lays out v_active_ids.
     *
     * @param v_active_ids list of buttons to actually put in the toolbar 
     */
    public void buildToolbar ( List<ButtonId> v_active_ids ) {
        setFloatable ( false );
        
        for ( ButtonId n_button : ButtonId.values () ) {            
            Action  act_button = null;
         
            switch ( n_button ) {
                case BACK: {
                    act_button = new AbstractAction () {
                    @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            if ( oi_nav_position > 1 ) {
                                --oi_nav_position;
                                UUID  u_nav = olist_navigation.get ( oi_nav_position - 1 );
                                otool_handler.fireLittleEvent ( new NavRequestEvent ( JSimpleAssetToolbar.this,
                                                                                      u_nav,
                                                                                      NavRequestEvent.NavMode.GENERIC
                                                                                      )
                                                                );
                                omap_button.get ( ButtonId.FORWARD ).setEnabled( true );
                                if ( 1 == oi_nav_position ) {
                                    omap_button.get ( ButtonId.BACK ).setEnabled( false );
                                }
                            }
                        }
                    };

                    act_button.setEnabled ( false );
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.back" ) );
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Navigate backward through nav history" );
                } break;
                case REFRESH: {
                    act_button = new AbstractAction () {
                        @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            otool_handler.fireLittleEvent ( new RefreshRequestEvent ( JSimpleAssetToolbar.this )
                                                                );
                        }
                    };

                    act_button.setEnabled ( true );
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.refresh" ) );
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Reload the view data from the repository" );
                } break;
                case UP: {
                    act_button = new AbstractAction () {
                    @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            if ( (null == oview_component)
                                    || (null == oview_component.getAssetModel())
                                    || (null == oview_component.getAssetModel().getAsset())
                                    )
                            {
                                // do nothing
                                return;
                            }

                            final Asset aNow = oview_component.getAssetModel().getAsset();
                            if ( aNow.getFromId() != null ) {
                                otool_handler.fireLittleEvent ( new NavRequestEvent ( JSimpleAssetToolbar.this,
                                                                                      aNow.getFromId(),
                                                                                      NavRequestEvent.NavMode.GENERIC
                                                                                      )
                                                                );
                            }
                        }
                    };

                    act_button.setEnabled ( true );
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.up" ) );
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Navigate up the asset tree (from-id)" );
                } break;

                case FORWARD: {
                    act_button = new AbstractAction () {
                        @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            if ( oi_nav_position < olist_navigation.size () ) {
                                ++oi_nav_position;
                                UUID  u_nav = olist_navigation.get ( oi_nav_position - 1 );
                                otool_handler.fireLittleEvent ( new NavRequestEvent ( JSimpleAssetToolbar.this,
                                                                                      u_nav,
                                                                                      NavRequestEvent.NavMode.GENERIC
                                                                                      )
                                                                );
                                omap_button.get ( ButtonId.BACK ).setEnabled( true );
                                if ( olist_navigation.size () == oi_nav_position ) {
                                    omap_button.get ( ButtonId.FORWARD ).setEnabled( false );
                                }
                            }
                        }
                    };
                    act_button.setEnabled ( false );
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.forward" ) );
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Navigate forward through nav history" );
                } break;
                case EDIT: {
                    act_button = new AbstractAction () {
                    @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            otool_handler.fireLittleEvent ( new EditRequestEvent ( JSimpleAssetToolbar.this,
                                                                                  oview_component.getAssetModel ()
                                                                                  )
                                                            );                            
                        }
                    };
                    act_button.setEnabled ( false );                    
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.edit" ) );                    
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Edit active Asset" );
                } break;
                case CREATE: {
                    act_button = new AbstractAction () {
                    @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            otool_handler.fireLittleEvent ( new CreateRequestEvent ( JSimpleAssetToolbar.this,
                                                                                     oview_component.getAssetModel ()
                                                                                   )
                                                            );                                  
                        }
                    };
                    act_button.setEnabled ( false );                    
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.addnew" ) );                    
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Create new Asset" );
                } break;
                case DELETE: {
                    act_button = new AbstractAction () {
                        @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            otool_handler.fireLittleEvent ( new DeleteRequestEvent ( JSimpleAssetToolbar.this,
                                                                                     oview_component.getAssetModel ()
                                                                                     )
                                                            );                                  
                        }
                    };
                    act_button.setEnabled ( false );                    
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.delete" ) );                    
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Delete Asset" );
                } break;                    
                case GOTO: {
                    act_button = new AbstractAction () {
                        /** Should do this in a spun off SwingWorker once we up to jdk 1.6 */
                    @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            String s_path = owtext_goto_path.getText ();
                            if ( s_path.trim().matches("^/?$" ) ) {
                                s_path = "/littleware.home";
                                owtext_goto_path.setText( s_path );
                            }
                            try {
                                AssetPath  path_goto = ofactoryPath.createPath ( s_path );
                                AssetModel model_goto = olib_asset.syncAsset ( om_search.getAssetAtPath ( path_goto ).get() );
                                ou_goto = model_goto.getAsset ().getObjectId ();
                                oview_component.setAssetModel ( model_goto );
                            } catch ( Exception e ) {
                                olog_generic.log ( Level.INFO, "Caught unexpected: " + e + ", " +
                                                   BaseException.getStackTrace ( e )
                                                   );
                                JOptionPane.showMessageDialog( null,
                                                               "Could not resolve asset at: " + s_path +
                                                               ", caught: " + e, "alert", 
                                                               JOptionPane.ERROR_MESSAGE
                                                               );                                            
                            }
                        }
                    };
                    act_button.setEnabled ( true );                    
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.goto" ) );
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Go to path" ); 
                    owtext_goto_path.addActionListener ( act_button );
                } break;
                    
                default: {
                    olog_generic.log ( Level.WARNING, "Unhandled ButtonId: " + n_button );
                    continue;
                }
            }
            
            JButton wbutton_tool = new JButton ( act_button );
            if ( n_button.equals ( ButtonId.GOTO ) ) {
                wbutton_tool.setText ( "GO" );
            }
            omap_button.put ( n_button, wbutton_tool );
        }
        
        // Now layout the buttons in the toolbar
        // No duplicates allowed
        final Set<ButtonId>  v_already = EnumSet.noneOf ( ButtonId.class );

        for ( ButtonId n_button : v_active_ids ) {
            if ( v_already.contains ( n_button ) ) {
                continue;
            }
            v_already.add ( n_button );
            
            if ( n_button.equals ( ButtonId.GOTO ) ) {
                this.addSeparator ();
                this.add ( owtext_goto_path );
            }
            this.add ( omap_button.get ( n_button ) );
        }
    }
    
    
    /**
     * Track when the view changes models
     */
    @Override
    public void propertyChange ( PropertyChangeEvent evt_nav ) {
        if ( evt_nav.getPropertyName ().equals ( AssetView.Property.assetModel.toString () )
             && (null != oview_component.getAssetModel ())
             ) {
            final Asset           aNav = oview_component.getAssetModel ().getAsset ();
            final UUID            u_nav = aNav.getObjectId ();
            
            if ( null != u_nav ) {
                if ( null == aNav.getFromId() ) {
                    omap_button.get( ButtonId.UP ).setEnabled(false);
                } else {
                    omap_button.get( ButtonId.UP ).setEnabled(true);
                }
                if ( (null == ou_goto) || (! u_nav.equals( ou_goto )) ) {
                    SwingUtilities.invokeLater ( new Runnable () {
                        @Override
                        public void run () {
                            ou_goto = u_nav;
                            AssetPath  path_new = ofactoryPath.createPath ( u_nav );
                            try {
                                path_new = ofactoryPath.toRootedPath( path_new );
                            } catch ( Exception ex ) {
                                olog_generic.log( Level.WARNING, "Failed to resolve root asset path for " + path_new, ex );
                            }
                            owtext_goto_path.setText ( path_new.toString () );
                        }
                    }
                                                 );
                }
                if ( (! olist_navigation.isEmpty ())
                     && olist_navigation.get ( oi_nav_position - 1 ).equals ( u_nav )
                     ) {
                    // nav to current position
                    return;
                }

                /** Remove the FORWARD part of the nav list */
                for ( int i_remove = olist_navigation.size () - 1; 
                      (i_remove >= oi_nav_position) && (i_remove >= 0);
                      --i_remove
                      ) {
                    olist_navigation.remove ( i_remove );
                }
                olist_navigation.add ( u_nav );
                /** Trim old history */
                for ( int i= getNavHistoryLimit ();
                      i < olist_navigation.size ();
                      ++i
                      ) {
                    olist_navigation.remove ( 0 );
                }
                oi_nav_position = olist_navigation.size ();                
                omap_button.get ( ButtonId.FORWARD ).setEnabled ( false );
                if ( oi_nav_position > 1 ) {
                    omap_button.get ( ButtonId.BACK ).setEnabled ( true );
                }
            }
        }
    }
    
    /**
     * Return unmodifiable unsyncrhonized view of the navigation history
     * as observed by this toolbar.
     */
    public List<UUID>  getNavHistory () {
        return Collections.unmodifiableList( olist_navigation );
    }
    
    /**
     * Get the current limit on the size of history tracked
     */
    public int getNavHistoryLimit () {
        return OI_HISTORY_LIMIT;
    }

    private static final List<ButtonId>  ov_all_buttons = new ArrayList<ButtonId> ();
    {
        Collections.addAll ( ov_all_buttons, ButtonId.values () );
    }
    
    /**
     * Constructor just injects dependencies.
     *
     * @param lib_asset asset data library
     * @param lib_icon source of icons
     * @param m_search to resolve lookups with
     */
    @Inject
    public JSimpleAssetToolbar ( 
                                 AssetModelLibrary lib_asset,
                                 IconLibrary lib_icon,
                                 AssetSearchManager m_search,
                                 AssetPathFactory factoryPath
                                 ) {
        super ( "toolbar" );
                
        olib_asset = lib_asset;
        olib_icon = lib_icon;
        om_search = m_search;
        ofactoryPath = factoryPath;
        buildToolbar ( ov_all_buttons );
    }
    
    
    
    
    /**
     * Get the button with the given id - so caller can
     * add her own listeners or whatever.
     *
     * @param n_button id of button to retrieve
     * @return the button or null if that button is not part of the toolbar
     */
    public JButton getButton ( ButtonId n_button ) {
        return omap_button.get ( n_button );
    }

    @Override
    public void	addLittleListener( LittleListener listen_little ) {
        otool_handler.addLittleListener ( listen_little );
    }
    
    
    @Override
    public void     removeLittleListener( LittleListener listen_little ) {
        otool_handler.removeLittleListener ( listen_little );
    }
    
    
    /**
     * Property tracks the view this toolbar is watching for 
     * PropertyChangeEvents on change to view model.
     */
    public AssetView getConnectedView () {
        return oview_component;
    }
    public void setConnectedView ( AssetView view ) {
        if ( null != oview_component ) {
            oview_component.removePropertyChangeListener( this );
        }
        oview_component = view;
        olist_navigation.clear();
        oi_nav_position = 0;
        if ( null != oview_component ) {
            oview_component.addPropertyChangeListener ( this );

            AssetModel model_view = oview_component.getAssetModel ();
            if ( null != model_view ) {
                olist_navigation.add ( model_view.getAsset ().getObjectId () );
                oi_nav_position = olist_navigation.size ();
            }
        }

    }
        
}

