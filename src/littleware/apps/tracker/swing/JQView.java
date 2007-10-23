package littleware.apps.tracker.swing;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.table.*;

import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.base.NoSuchThingException;
import littleware.apps.tracker.*;
import littleware.apps.swingclient.*;


/** 
 * Swing based AssetView for Queue type assets.
 */
public class JQView extends JGenericAssetView {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.apps.tracker.swing.JQView" );
    // Should use this as key into multilanguage resource bundle eventually
    private final static String[]      ov_columns = {
        "Id", 
        "Task Status",
        "Creator",
        "Create Date",
        "Comment",
        "Last update",
        "Updater",
        "Update comment"
    };
    
    /** Display no more than 100 tasks in queue */
    public static final int   OI_MAX_TASK = 100;
    
    /** Order in which we want to view tasks in the queue */
    private static final TaskStatus[]  ov_status_order = { 
        TaskStatus.PROCESSING,
        TaskStatus.STALLED,
        TaskStatus.WAITING_IN_Q, 
        TaskStatus.FAILED, 
        TaskStatus.DONE,
        TaskStatus.IDLE,
        TaskStatus.WAITING_ON_TASK
    };
    
    private final AssetSearchManager       om_search;
    private final IconLibrary              olib_icon;
    /** Task currently visible in the right-panel display */
    private final java.util.List<AssetModel>     ov_tasks = new ArrayList<AssetModel> ();
    /** Table model hooked up with ov_tasks under the hood */
    private final AbstractTableModel       omodel_tasks = new AbstractTableModel() {
        public String getColumnName(int i_col) {
            return ov_columns[i_col];
        }
        public int getRowCount() { return ov_tasks.size (); }
        public int getColumnCount() { return ov_columns.length; }
        public Object getValueAt(int i_row, int i_col) {
            AssetModel  model_display = ov_tasks.get ( i_row );
            Task        task_display = (Task) model_display.getAsset ();
            try {
                switch ( i_col ) {
                    case 0: { // Task id link
                        return task_display;
                    } 
                    case 1: { // Name
                        return task_display.getTaskStatus ();
                    } 
                    case 2: { // Creator
                        return getAssetModel ().getLibrary ().retrieveAssetModel ( task_display.getCreatorId (), om_search ).getAsset ().getName ();
                    } 
                    case 3: { // Create date
                        return task_display.getCreateDate ();
                    } 
                    case 4: { // Comment
                        return task_display.getComment ();
                    } 
                    case 5: { // Last update date
                        return task_display.getLastUpdateDate ();
                    } 
                    case 6: { // Last updater
                        return getAssetModel ().getLibrary ().retrieveAssetModel ( task_display.getLastUpdaterId (), om_search ).getAsset ().getName ();   
                    } 
                    case 7: { // Last update comment
                        return task_display.getLastUpdate ();
                    } 
                    default: {
                        return "DEFAULT ERROR";
                    }
                }
            } catch ( Exception e ) {
                olog_generic.log ( Level.WARNING, "UI processing error, caught: " + e );
                return e;
            }
        }
        
        public Class<?> getColumnClass ( int i_column ) {
            if ( 0 == i_column ) {
                return UUID.class;
            }
            return Object.class;
        }
                    
        public boolean isCellEditable(int row, int col) { return false; }
    };
    
    private final JTable       owtable_tasks = new JTable ( omodel_tasks );;
    private final JPanel       owpanel_right = new JPanel ( new BorderLayout () );
    
    {
        this.addPropertyChangeListener ( new PropertyChangeListener () {

                /** Receive events from the View model */
                public void propertyChange ( PropertyChangeEvent evt_prop ) {
                    if ( evt_prop.getPropertyName ().equals ( AssetView.Property.assetModel.toString () ) ) {
                        // Model has changed under us
                        updateQData ();
                    }
                }
            }
        );              
    }
    
    
    /**
     * Internal utility to configure our table customization stuff bla bla.
     * Constructor needs to call this.
     */
    private void configureTable () {
        TableCellRenderer  render_datecell = new DefaultTableCellRenderer () {
            DateFormat oformat_date = new SimpleDateFormat ( "yyyy/MM/dd" );
            
            public void setValue( Object x_value ) {
                if ( null == x_value ) {
                    setText ( "" );
                } else if ( x_value instanceof java.util.Date ) {
                    setText( oformat_date.format( (Date) x_value) );
                } else {
                    setText ( x_value.toString () );
                }
            }
        };
        
        TableColumn column_info = null;
        for (int i = 0; i < ov_columns.length; i++) {
            column_info = owtable_tasks.getColumnModel().getColumn(i);
            if ( 0 == i ) {
                column_info.setPreferredWidth( 200 ); // id column
            } else if ( (4 == i) || (7 == i) ) {
                column_info.setPreferredWidth( 300 ); //comment column is bigger
            } else {
                column_info.setPreferredWidth( 100 );
            }
            if ( 0 == i ) {
                column_info.setCellRenderer ( new JAssetLink ( olib_icon, true ) );
            } else if ( (3 == i) || (5 == i) ) {
                column_info.setCellRenderer ( render_datecell );
            }
        }
        

        owtable_tasks.setDefaultRenderer ( java.util.Date.class, render_datecell );
        owtable_tasks.setAutoResizeMode ( JTable.AUTO_RESIZE_OFF );
        
        JScrollPane wscroll_table = new JScrollPane( owtable_tasks );
        owtable_tasks.setPreferredScrollableViewportSize(new Dimension(600, 80));
        owpanel_right.add ( wscroll_table, BorderLayout.CENTER );
    }
        
    
    /**
     * Constructor sets up the UI to view the given queue
     *
     * @param model_queue to view
     * @param m_search to retrieve asset details with
     * @param lib_icon icon source
     * @exception IllegalArgumentException if model_queue does not reference a Queue
     */
    public JQView( AssetModel model_queue, AssetSearchManager m_search,
                       IconLibrary lib_icon 
                       ) throws BaseException, GeneralSecurityException, RemoteException 
    {
        super( model_queue, m_search, lib_icon );
        om_search = m_search;
        olib_icon = lib_icon;
        configureTable ();
        updateQData ();
        {
            GridBagConstraints  grid_right = new GridBagConstraints ();
            
            olog_generic.log ( Level.FINE, "Adding right panel to display" );
            grid_right.gridx = GridBagConstraints.RELATIVE;
            grid_right.gridy = 0;
            grid_right.gridwidth = 1;
            grid_right.gridheight = GridBagConstraints.REMAINDER;
            grid_right.fill = GridBagConstraints.BOTH;
            grid_right.anchor = GridBagConstraints.FIRST_LINE_END;
            
            this.add ( owpanel_right, grid_right );
        }
    }    
                       

    
	public void updateQData ()
    {
        ov_tasks.clear ();
        
        int    i_count = 0;
        if ( null == getAssetModel () ) {
            return;
        }
        littleware.apps.tracker.Queue  q_view = (littleware.apps.tracker.Queue) getAssetModel ().getAsset ();
        try {
            for ( TaskStatus n_status : ov_status_order ) {
                if ( i_count > OI_MAX_TASK ) {
                    break;
                }
                
                java.util.List<UUID>  v_ids = q_view.getTask ( n_status );
                for ( UUID u_id : v_ids ) {
                    ++i_count;
                    ov_tasks.add ( getAssetModel ().getLibrary ().retrieveAssetModel ( u_id, om_search ) );
                    if ( i_count > OI_MAX_TASK ) {
                        break;
                    }                
                }
            }
        } catch ( Exception e ) {
            // Need to introduce an exception DIALOG here
            olog_generic.log ( Level.WARNING, "Caught exception retrieving task info: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
        }
        omodel_tasks.fireTableDataChanged ();
    }
 
	protected void eventFromModel ( LittleEvent event_from_model )
    {        
        if ( event_from_model.getSource () != getAssetModel () ) {
            return;
        }
        
        updateQData ();
        super.eventFromModel ( event_from_model ); 
    }
    
}