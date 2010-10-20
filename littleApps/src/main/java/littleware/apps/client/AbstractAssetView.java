/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.client;

import littleware.base.feedback.SimpleLittleTool;
import littleware.base.feedback.LittleListener;
import littleware.base.feedback.LoggerFeedback;
import littleware.base.feedback.LittleEvent;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.base.feedback.Feedback;



/** 
 * Basic UI-independent implementation of AssetView interface methods.
 * Intended for delegation by Swing-based AssetView implementors.
 */
public abstract class AbstractAssetView extends SimpleLittleTool implements AssetView {
    private final static Logger    olog_generic = Logger.getLogger (AbstractAssetView.class.getName() );
    
    private AssetModel          omodel_asset = null;
    private AssetModelLibrary   olib_asset = null;
    private Feedback            ofeedback = new LoggerFeedback( olog_generic );
    
    /** Bridge propagate events from AssetModel */
    private final LittleListener olisten_bridge = new LittleListener () {
        @Override
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
    
    @Override
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
    @Override
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

    @Override
    public Feedback getFeedback() {
        return ofeedback;
    }
    @Override
    public void  setFeedback( Feedback feedback ) {
        ofeedback = feedback;
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

