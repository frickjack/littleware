/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * littleware.babyTrack.view.contractionPanel module 
 * assembles the sub-panels that make up the core
 * contraction-app UX.
 * Typical use:  
 * <ul>
 *   <li>var app = contraction.Factory.get().container( node 
 *         ).model( contractionList 
 *         ).controller( controller ).build()
 *   <li>app.render();
 * </ul>
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.babyTrack.view.contractionPanel
 * @namespace littleware.babyTrack.view.contractionPanel
 */
YUI.add( 'littleware-babyTrack-view-contractionPanel', function(Y) {
    Y.namespace('littleware.babyTrack.view.contractionPanel');
    Y.littleware.babyTrack.view.contractionPanel = (function() {
        var model = Y.littleware.babyTrack.model;
        var view = Y.littleware.babyTrack.view;
        var util = Y.littleware.littleUtil;
        var log = new util.Logger( "littleBabyTrack" );

        /**
         * Internal utility - pad a positive integer to at least length 2 with a zero
         * @method zPad
         * @param {int} i
         * @return {string} i padded to length >= 2 with zero prefix if necessary
         */
        var zPad = function( i ) {
            if( i < 10 ) {
                return "0" + i;
            }
            return "" + i;
        };
        
        /**
         * Internal utility - format a date down to minute: HH:MI
         * @method dateMinuteFormatter
         * @param {Date} date
         */
        var dateMinuteFormatter = function( date ) {
            var hour = date.getHours() % 12;
            hour = hour || 12;
            var minute = date.getMinutes();
            return "" + hour + ":" + zPad( minute );
        }
        
        /**
         * Internal uitility - format date to second: HH:MI:SS
         * @method dateSecondFormatter
         * @param {Date} date
         */
        var dateSecondFormatter = function( date ) {
            var second = date.getSeconds();
            return dateMinuteFormatter( date ) + ":" + zPad( second );
        }

        /**
         * View class extends Y.View
         * @class View
         * @constructor
         */
        var View = Y.Base.create( "btrackMainView", Y.View, [], {
            //container:"",
            events:{
            },
            
            template: "", // assume html template is pre-rendered for us for now
            
            initializer:function(){
                
                var dateFormatter = function( o ) { return dateSecondFormatter( o.value ); }
                
                this.dataTable = new Y.DataTable( {
                    columns: [ 
                        "id", 
                        { key:"startTime", 
                            label:"start",
                            formatter:dateFormatter
                        },
                        { key:"endTime", 
                            label:"end",
                            formatter:dateFormatter
                        },                        
                        {
                            key:"seconds",
                            label:"duration",
                            formatter:function(o) {
                                return "" + (Math.floor( o.value / 60 )) + ":" + zPad( o.value % 60);
                            }
                        } 
                    ],
                    data: this.get( "model" ).get( "history" )
                  }
                );
                    
                this.buttonPanel = view.startStopButtonPanel.Factory.get().build();

                
                this.chart = new Y.Chart( {
                    dataProvider: this.buildChartModel(),
                    //render: "#graph"
                    type: "combo",
                    showAreaFill:true,
                    categoryKey:"time", 
                    //categoryType:"time",
                    tooltip:{ show:false },                    
                    axes: {
                        time: {
                            labelFunction:function( value ) {
                                // only display 5-minute values
                                if ( value.getMinutes() % 5 == 0 ) {
                                    return dateMinuteFormatter( value );
                                } else {
                                    //log.fine( "Chart label ignoring date: " + dateMinuteFormatter( value ) );
                                    return "";
                                }
                            }
                        },                        
                        values: {
                            labelFunction:function( value ) {
                                // only display full int values
                                if ( Math.floor( value ) != value ) {
                                    return "";  
                                } else {
                                    return "" + value;
                                }
                            },
                            minimum: 0,
                            maximum: 5
                        }
                    },
                    styles: {
                                axes: { 
                                        time: { 
                                                label: {
                                                        rotation: -45
                                                }
                                        }
                                }
                        }
                });
            },
            
            /**
             * Internal utility assembles a "dataProvider" suitable 
             * for the Y.Chart based "chart" view
             * from the main modelList (this.model) tracking contractions
             * @method buildChartModel
             * @return {Array} dataProvider array
             */
            buildChartModel: function() {
                var appModel = this.get( "model" );
                var history = appModel.get( "history" );
                var chart = this.chart;
                var msInMin = 60*1000;
                var now = new Date();
                
                if( ! this.startTime ) {
                    this.startTime = new Date( Math.floor( now.getTime() / msInMin) * msInMin );
                }
                var startTime = this.startTime;

                // reset start-time if older than 50 minutes
                if ( (now.getTime() - 50 * msInMin) > startTime.getTime() ) {
                    startTime = new Date( now.getTime() - 40*msInMin );
                }
                var contractionMinutes = 0; // how long in current contraction ?
                var i=0;
                var j=history.size() - 1;
                var currentContractionStart = null;
                if ( appModel.get( "state" ) == "in" ) {
                    currentContractionStart = appModel.builder.startTime;
                }
                var chartData = [];
                log.fine( "Considering: " + history.size() + " contractions and current start: " + currentContractionStart );
                for( i=0; i < 60; ++i ) { // plot a mark for each of 60 minutes
                    var t = new Date( startTime.getTime() + i*60*1000 );
                    // are we in a contraction ?
                    while( j >= 0 && (history.item(j).get( "endTime" ).getTime() < t.getTime()) ) {
                        // history has most recent contraction first, and oldest last
                        --j;
                    }
                    
                    // accumulate the number of minutes in each contraction
                    if( j >= 0 && 
                        (history.item(j).get( "startTime" ).getTime() < t.getTime() + 60000 ) 
                    ) {
                        contractionMinutes += 1;
                    } else if ( (j < 0) && currentContractionStart && 
                        currentContractionStart.getTime() < t.getTime() &&
                        currentContractionStart.getTime() > t.getTime() - 60000
                       ) {
                        contractionMinutes += 1;
                    } else {
                        contractionMinutes = 0;
                    }
                    // clamp at 5
                    if ( contractionMinutes > 5 ) {
                        contractionMinutes = 5; 
                    }
                    chartData.push( { time:t, value:contractionMinutes} );
                }
                return chartData;
            },
                        
            /**
             * Override render
             */
            render:function() {
                log.info( "Rendering contraction panel ..." );
                var root = this.get( "container" );
                
                var tableDiv = root.one( "div.dataTable" );
                tableDiv.setHTML( "" );
                this.dataTable.render( tableDiv );
                
                var chartDiv = root.one( "figure" );
                chartDiv.setHTML( "" );
                this.chart.render( chartDiv );
                
                var buttonContainer = root.one( "#button" );
                buttonContainer.setHTML( "" );
                this.buttonPanel.set( "container", buttonContainer  );
                this.buttonPanel.render();
            }
        } /* ,{ ATTRS:{} } */
            );
        
        /**
         * Main view builder with container, model, and controller properties
         * @class Builder
         * @constructor
         */
        var Builder = Y.Base.create( "cpBuilder", util.BuilderBuilder.create( ["container", "model", "controller"] ), [], {
        
            build : function() {
                this.container = this.container || Y.one( "#app" );
                this.model = this.model || model.contractionApp.Factory.get();
                this.controller = this.controller || new Controller();
                util.assert( this.validate().length < 1, "all builder properties set: " + this.validate() );
            
                var view = new View( {
                   container:this.container,
                   model:this.model
                });
                view.controller = this.controller;
                this.controller.view = view;
                this.controller.model = this.model;
                var buttonModel = view.buttonPanel.get( "model" );
                buttonModel.after( "stateChange", function(ev) {
                    view.controller.toggleContraction( ev );
                } );
                return view;
            }
        });    
        
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
         */
        var Controller = function() {
            this.model = null;
            this.view = null;
        }
        
        /**
         * Record new contraction in response to start/stop contraction button clicks
         * @method toggleContraction
         */
        Controller.prototype.toggleContraction = function(ev) {
            var state = this.view.buttonPanel.get( "model" ).get( "state" )
            log.info( "Toggle button toggled to: " + state );
            util.assert( this.model && this.view, "Controller model and view intialized" );
            
            if( "running" == state ) {
                this.model.startContraction();
            } else {
                this.model.endContraction();
            }
            
            // this is a goofy place to do this ...
            this.view.chart.set( "dataProvider", this.view.buildChartModel() );
            //this.view.chart.render();
        }
        
        
        //----------------------------------
        
        return {
            Factory:Factory
        };
    }
  )();
}, '0.1.1' /* module version */, {
    requires: [ 'base', 'charts', 'datatable', 
        'littleware-babyTrack-model-contractionApp', 
        'littleware-babyTrack-view-startStopButtonPanel', 
        'littleware-littleUtil', "model-list", 'view' ]
});



