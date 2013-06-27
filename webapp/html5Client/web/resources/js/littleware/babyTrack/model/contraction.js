/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * littleware.babyTrack.model.contraction module provides
 * simple read-only ContractionInfo object with Factory and Builder
 * support classes for assembling the info.
 * Typical use:  
 * <ul>
 *   <li>var taskBar = contraction.Factory.get().startTime( dateStart ).endTime( endDate ).build()
 * </ul>
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.babyTrack.model.contraction
 * @namespace littleware.babyTrack.model.contraction
 */
YUI.add( 'littleware-babyTrack-model-contraction', function(Y) {
    Y.namespace('littleware.babyTrack.model.contraction');
    Y.littleware.babyTrack.model.contraction = (function() {
        var util = Y.littleware.littleUtil;
        var log = new util.Logger( "littleBabyTrack" );

        /**
         * Read-only contraction object just tracks startTime, endTime
         * Date-type properties.  Allocated via Builder acquired by Factory.get()
         * @class Contraction
         * @constructor
         * @param {} props hash of prop
         */
        var Contraction = function( props ) {
            for( var key in props ) {
                this[key] = props[key];
            }
            util.assert( this.startTime && this.endTime &&
                this.endTime.getTime() > this.startTime.getTime(),
                "start/end time properties set" 
            );
            return this;
        };
        
        /**
         * Contraction builder with startTime/endTime setters
         * @class Builder
         * @constructor
         */
        var Builder = util.BuilderBuilder.create( [ "startTime", "endTime" ] );
        
        
        var id=0;
        
        /**
         * @method build
         */
        Builder.prototype.build = function() {
            util.assert( this.validate().length == 0, "Builder properties all set" );
            return new Contraction( {
               id:id++,
               startTime:this.startTime,
               endTime:this.endTime,
               seconds: Math.floor( (this.endTime.getTime() - this.startTime.getTime()) / 1000 )
            });
        }
        
        /**
         * @class Factory
         */
        var Factory = {
            /**
             * @method get
             * @static
             */
            get:function() { return (new Builder()).withStartTime( new Date() ); }
        };
        
        //------------------------------------
        
        
        return {
            Factory:Factory
        };
    }
  )();
}, '0.1.1' /* module version */, {
    requires: []
});

