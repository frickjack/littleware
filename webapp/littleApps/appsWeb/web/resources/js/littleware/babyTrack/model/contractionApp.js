/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * littleware.babyTrack.model.contractionApp tracks the
 * state of the contraction-tracking application.
 * Just has a couple of attributes:
 * <ul>
 *     state - in or between contractions
 *     history - modelList of contraction.Contraction objects
 * </ul>
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.babyTrack.model.contractionApp
 * @namespace littleware.babyTrack.model.contractionApp
 */
YUI.add( 'littleware-babyTrack-model-contractionApp', function(Y) {
    Y.namespace('littleware.babyTrack.model.contractionApp');
    Y.littleware.babyTrack.model.contractionApp = (function() {
        var model = Y.littleware.babyTrack.model;
        var util = Y.littleware.littleUtil;
        var log = new util.Logger( "littleBabyTrack" );
        
        
        /**
         * AppModel tracks application state
         * @class AppModel
         * @constructor
         */
        var AppModel = Y.Base.create( 'contractionAppModel', Y.Model, [],
                 {
                     startContraction:function(){
                         this.set( "state", "in" );
                         this.builder = model.contraction.Factory.get();
                     },
                     endContraction:function(){
                         if ( this.builder ) {
                           this.builder.withEndTime( new Date() );
                           var history = this.get( "history" );
                           history.add( this.builder.build(), { index:0 } );
                           this.builder = undefined;
                         }
                         this.set( "state", "between" );
                     }
                 },
                 {
                     ATTRS: {
                         state: {
                             value: "between"
                         },
                         history: {
                             value: new Y.ModelList(),
                             readOnly:true
                         }
                     }
                 }
                );
                    
        
        /**
         * AppModel Factory
         * @class Factory
         */
        var Factory = {
            /**
             * @method get
             * @static
             * @return {AppModel}
             */
            get:function() { return new AppModel(); }
        };
        
        //------------------------------------
        
        
        return {
            Factory:Factory
        };
    }
  )();
}, '0.1.1' /* module version */, {
    requires: [ 'base', 'event', 'littleware-babyTrack-model-contraction', 'model', 'model-list', 'node' ]
});



