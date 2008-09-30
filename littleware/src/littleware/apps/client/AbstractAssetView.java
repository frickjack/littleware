package littleware.apps.client;

import javax.swing.SwingUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.AssertionFailedException;


/** 
 * Basic UI-independent implementation of AssetView interface methods.
 * Intended for delegation by Swing-based AssetView implementors.
 */
public abstract class AbstractAssetView extends SimpleLittleTool implements AssetView {
    private final static Logger    olog_generic = Logger.getLogger ( "littleware.apps.swingclient.AbstractAssetView" );
    
    private AssetModel          omodel_asset = null;
    private AssetModelLibrary   olib_asset = null;
    
    /** Bridge propagate events from AssetModel */
    private final LittleListener olisten_bridge = new LittleListener () {
        public void receiveLittleEvent ( LittleEvent evt_model ) {
            olog_generic.log ( Level.FINE, "Propogating event: " + evt_model.getOperation () );
            eventFromModel ( evt_model );
        }
    };
    
    /**
     * Constructor takes the bean to use as the source of any thrown events
     */
    public AbstractAssetView ( Object x_sourcebean ) {
        super ( x_sourcebean );
    }
    
    public AssetModel getAssetModel () {
        return omodel_asset;
    }
    
    /**
     * Set the model, register as a listener of the new model,
     * unregister as a listener of the old,
     * and fire an an AssetEvent with SWAP_MODEL op
     * to the listeners registered with this class.
     * NOOP if model_asset is NULL or same as already assigned AssetModel.
     */
    public void setAssetModel ( AssetModel model_asset ) {
        if ( (null == model_asset)
             || (model_asset == omodel_asset)
             ) {
            return;
        }
        if ( (null != olib_asset)
             && (olib_asset != model_asset.getLibrary ()) ) {
            throw new LibraryMismatchException ( "May not switch view between AssetModelLibrary" );
        }
        olib_asset = model_asset.getLibrary ();
        
        if ( null != omodel_asset ) {
            omodel_asset.removeLittleListener ( olisten_bridge );
        }
        
        AssetModel  model_old = omodel_asset;
        
        omodel_asset = model_asset;
        omodel_asset.addLittleListener ( olisten_bridge );
        firePropertyChange ( AssetView.Property.assetModel.toString (), model_old, model_asset );
    }
        

    /**
     * Convenience method bridges property-change events from the active
     * model under the view.
     *
     * @param evt_model event from the AssetModel this View is observing
     *              or whatever else might be observed by subtypes
     */
    protected abstract void eventFromModel ( LittleEvent evt_model );
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

