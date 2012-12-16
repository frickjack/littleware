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
                         // history is model list in reverse time order
                         var history = appModel.get( "history" );
                         var start = null;
                         var period = [];
                         var duration = [];
                         var end = null;
                         history.each( function( sample ) {
                             var t0 = sample.get( "startTime" );
                             var t1 = sample.get( "endTime" );
                             duration.push( Math.floor( (t1.getTime() - t0.getTime()) / 1000 ) );
                             if( null == end ) {
                                 end = t1;
                             } else {
                                 period.push( Math.floor( (start.getTime() - t0.getTime()) / 1000 ) );
                             }
                             start = t0;
                         });
                         
                         var average = function( vec ) {
                             var sum = 0;
                             for( var i=0; i < vec.length; ++i ) {
                                 sum += vec[i];
                             }
                             if ( vec.length < 1 ) {
                                 return 0;
                             }
                             return sum / vec.length;
                         }
                         
                         if ( start && end ) {
                            this.set( "aveDuration", Math.floor( average( duration ) ) );
                            this.set( "avePeriod", Math.floor( average( period ) ) );
                            this.set( "totalTime", Math.floor( (end.getTime() - start.getTime()) / 1000 ));
                         }
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




