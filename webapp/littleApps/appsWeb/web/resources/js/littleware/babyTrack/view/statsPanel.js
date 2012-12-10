/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * littleware.babyTrack.view.statsPanel module 
 * provides a View class that displays the contraction-related
 * stats (average duration, spacing, and total time)
 * tracked in the panel's Model class.
 * The overall application controller can listen for data-change
 * events, and update the statsPanel's Model.
 * <ul>
 *   <li>var panel = statsPanel.Factory.get().container( node 
 *         ).build()
 *   <li>var model = panel.get( "model" );
 *   <li>model.updateStats( arrayOfDurations );
 *   <li>panel.render();
 * </ul>
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.babyTrack.view.statsPanel
 * @namespace littleware.babyTrack.view.statsPanel
 */
YUI.add( 'littleware-babyTrack-view-statsPanel', function(Y) {
    Y.namespace('littleware.babyTrack.view.statsPanel');
    Y.littleware.babyTrack.view.statsPanel = (function() {
        var util = Y.littleware.littleUtil;
        var log = new util.Logger( "littleBabyTrack" );

        //----------------------------------
    
    
    /**
     * Little model tracks contraction stats
     * @class Model
     */
    var Model = Y.Base.create( 'contractionStatsModel', Y.Model, [],
                 {
                     /**
                      * Little utility - updates attributes based on
                      * the history data in the given applicaiton model -
                      * only considers contractions that took place in the 
                      * last 60 minutes.
                      * @param {contractionApp.AppModel} appModel
                      */
                     updateStats:function( appModel ){
                         // bogus place-holder for initial testing
                         Y.Array.each( [ "aveDuration", "avePeriod", "totalTime" ],
                             function( key ) {
                                 this.set( key, this.get( key ) + 1 );
                             }, this );
                     }
                 },
                 {
                     ATTRS: {
                        /** 
                         * @attribute {int} aveDuration of a contraction in seconds
                         */                         
                         aveDuration: {
                             value: 0
                         },
                         /**
                          * @attribute {int} avePeriod between contractions in seconds
                          */
                         avePeriod: {
                             value: 0
                         },                         
                         /**
                          * @attribute {int} totalTime
                          */
                         totalTime: {
                             value: 0
                         }
                     }
                 }
                );        
        
        return {
            Model:Model
        };
    }
  )();
}, '0.1.1' /* module version */, {
    requires: [ 'base', 'littleware-littleUtil', "model", 'view' ]
});




