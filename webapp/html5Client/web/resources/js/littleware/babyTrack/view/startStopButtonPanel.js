/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * littleware.babyTrack.view.startStopButtonPanel module 
 * assembles a panel with a start/stop button.
 * Manages its own internal model and controller.
 * Larger applications can listen for events on the view.model
 * Typical use:  
 * <ul>
 *   <li>var panel = startStopButtonPanel.Factory.get().container( node 
 *         ).build()
 *   <li>var model = panel.get( "model" );
 *   <li>panel.render();
 * </ul>
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.babyTrack.view.startStopButtonPanel
 * @namespace littleware.babyTrack.view.startStopButtonPanel
 */
YUI.add( 'littleware-babyTrack-view-startStopButtonPanel', function(Y) {
    Y.namespace('littleware.babyTrack.view.startStopButtonPanel');
    Y.littleware.babyTrack.view.startStopButtonPanel = (function() {
        var util = Y.littleware.littleUtil;
        var log = new util.Logger( "littleBabyTrack" );

        
        /**
         * Start/stop button model with state, startTime, endTime properties
         * @class Model
         */
        var Model = Y.Base.create( "startStopModel", Y.Model, [], {
            /**
             * Update the state, startTime, endTime attributes
             * to tigger start of stopwatch
             * @method start
             */
            start: function() {
                var state = this.get( "state" );
                //util.assert( state == "waiting", "Attempt to start running stopwatch" );
                this.setAttrs( {
                    state:"running",
                    startTime: new Date(),
                    endTime: null
                });
            },
            
            /**
             * Update the state, startTime, endTime attributes
             * to tigger end of stopwatch
             * @method start
             */            
            stop: function() {
                var state = this.get( "state" );
                //util.assert( state == "running", "Attempt to stop waiting stopwatch" );
                this.setAttrs( {
                    state:"waiting",
                    endTime: new Date()
                });                
            }
        }, {
            ATTRS: {
                /**
                 * Possible states: waiting and running
                 * @attribute state
                 */
                state: {
                    value:"waiting"
                },
                /** @attribute startTime */
                startTime: {
                    value:null
                },
                /** @attribute endTime */
                endTime: {
                    value: null
                }
            }
        }
        );
            
        //---------------------------------
        
        /**
         * View class extends Y.View
         * @class View
         * @constructor
         */
        var View = Y.Base.create( "startStopView", Y.View, [], {
            events:{
                'span.button':{
                    click: "toggleButton"
                }
            },
        
            container: "div#button",
            template: "<span class=\"button buttonStart\">{label}</span>", // assume html template is pre-rendered for us for now
            
            initializer:function(){
            },
            
            /**
             * Event handler delegates out to attached controller
             * @method toggleContraction
             */
            toggleButton: function(ev) {
                this.controller.toggleButton();
            },
                        
            /**
             * Override render
             */
            render:function() {
                log.info( "Rendering buttonPanel ..." );
                var root = this.get( "container" );
                var content = Y.Lang.sub( this.template, { label:this.get( "startLabel" ) } );
                log.info( "Button panel rendering: " + content );
                root.setHTML( content );
                //alert( "Frickjack!" );
                this.syncState();
            },
            
            /**
             * Update UI after state-change on underlying model.
             */
            syncState : function ( ) {
                var model = this.get( "model" );
                util.assert( model , "View model intialized" );
                var buttonNode = this.get( "container" ).one( "span.button" );
                util.assert( buttonNode, "Found button node" );
                //if( buttonNode ) {  // render() has been called
                    if( "waiting" == model.get( "state" ) ) {
                        buttonNode.removeClass( "buttonStop" ).addClass( "buttonStart" 
                                ).set( 'text', this.get( "startLabel" ));;
                    } else {
                        buttonNode.removeClass( "buttonStart" ).addClass( "buttonStop" 
                            ).set( 'text', this.get( "stopLabel" ));
                    } 
                //} 
            }
                        
        } ,{ 
            ATTRS:{
                /**
                 * @attribute startLabel
                 * @default {String} "start"
                 */
                startLabel: {
                    value:"start contraction"
                },
                /**
                 * @attribute stopLabel
                 * @default {String} "stop"
                 */
                stopLabel: {
                    value:"stop contraction"
                },
                
                /**
                 * state is either "running" or "waiting" 
                 * @attribute state
                 */
                state: {
                    value:"waiting"
                }
            } 
        } );
        
        /**
         * Main view builder with container and controller properties
         * @class Builder
         * @constructor
         */
        var Builder = Y.Base.create( "startStopBuilder", 
             util.BuilderBuilder.create( [ "container", "controller" ] ), [],
             {
                 build: function() {
                     var view = new View( {
                         model:new Model()
                     });
                     var controller = this.controller || new Controller( view );
                     view.controller = controller;
                     return view;
                 }
             }
         );
        
        
        /**
         * @class Factory
         */
        var Factory = {
            /**
             * @method get
             * @static
             */
            get:function() { return new Builder(); }
        };
        
        //------------------------------------
        
        /**
         * Controller attached to the View hadles UX events.
         * @class Controller
         * @param {View} view to attach to
         */
        var Controller = function( view ) {
            this.view = view;
            this.model = view.get( "model" ); 
            this.model.after( "change", this.view.syncState, this.view );
        }
        
        
        /**
         * Update model in response to start/stop contraction button clicks
         * @method toggleContraction
         */
        Controller.prototype.toggleButton = function(ev) {
            log.info( "Toggle button toggled!" );
            util.assert( this.model && this.view, "Controller model and view intialized" );
            var buttonNode = this.view.get( "container" ).one( "span.button" );
            if( "waiting" == this.model.get( "state" ) ) {
                this.model.start();
            } else {
                this.model.stop();
            }            
        }
        
        
        //----------------------------------
        
        return {
            Factory:Factory
        };
    }
  )();
}, '0.1.1' /* module version */, {
    requires: [ 'base', 'littleware-littleUtil', "model", 'view' ]
});


