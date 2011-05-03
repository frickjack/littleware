/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import littleware.asset.client.AssetRef;
import littleware.base.feedback.LoggerFeedback;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleListener;
import littleware.base.event.helper.SimpleLittleTool;
import littleware.base.feedback.Feedback;



/** 
 * Basic UI-independent implementation of AssetView interface methods.
 * Intended for delegation by Swing-based AssetView implementors.
 */
public abstract class AbstractAssetView extends SimpleLittleTool implements AssetView {
    private final static Logger    log = Logger.getLogger (AbstractAssetView.class.getName() );
    
    private AssetRef          omodel_asset = null;
    private Feedback            ofeedback = new LoggerFeedback( log );
    
    /** Bridge propagate events from AssetRef */
    private final LittleListener olisten_bridge = new LittleListener () {
        @Override
        public void receiveLittleEvent ( LittleEvent evt_model ) {
            log.log ( Level.FINE, "Propogating event: {0}", evt_model.getClass().getName());
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
    public AssetRef getAssetModel () {
        return omodel_asset;
    }
    
    /**
     * Set the model, register as a listener of the new model,
     * unregister as a listener of the old,
     * and fire an an AssetEvent with SWAP_MODEL op
     * to the listeners registered with this class.
     * NOOP if model_asset is NULL or same as already assigned AssetRef.
     */
    @Override
    public void setAssetModel ( AssetRef model_asset ) {
        if ( (null == model_asset)
             || (model_asset == omodel_asset)
             ) {
            return;
        }
        
        if ( null != omodel_asset ) {
            omodel_asset.removeLittleListener ( olisten_bridge );
        }
        
        AssetRef  model_old = omodel_asset;
        
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
     * @param evt_model event from the AssetRef this View is observing
     *              or whatever else might be observed by subtypes
     */
    protected abstract void eventFromModel ( LittleEvent evt_model );
}

