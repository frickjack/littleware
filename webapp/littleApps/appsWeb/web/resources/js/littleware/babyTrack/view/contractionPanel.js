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
                
                var dateFormatter = function( o ) { return util.Date.secondString( o.value ); }
                
                this.dataTable = new Y.DataTable( {
                    columns: [ 
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
                                return "" + (Math.floor( o.value / 60 )) + ":" + util.zPadInt( o.value % 60);
                            }
                        } 
                    ],
                    data: this.get( "model" ).get( "history" )
                  }
                );
                
                var durationFormatter = function( o ) {
                    var secs = o.value;
                   return "" + Math.floor( secs / 60 ) + ":" + util.zPadInt( secs % 60 ); 
                };
                
                var startStopTotalFormatter = function( o ) {
                    var info = o.value;
                    return "" + util.Date.minuteString( info.startTime || new Date() ) +
                         " to " + util.Date.minuteString( info.endTime || new Date() ) + 
                         ", total: " + durationFormatter( { value: info.total });
                         
                }
                
                // Just use a Y.DataTable for the stats for now
                this.statsTable = new Y.DataTable( {
                 recordType:Y.littleware.babyTrack.view.statsPanel.Model,
                 columns: [
                    {
                        key:"avePeriod",
                        label: "Ave. Period",
                       formatter: durationFormatter
                    },
                    {
                       key:"aveDuration",
                       label: "Ave. Duration",
                       formatter: durationFormatter
                    },                    
                    {
                        key:"totalTime",
                        label: "Time Covered",
                       formatter: startStopTotalFormatter
                    }
                 ],
                 data:[
                    new Y.littleware.babyTrack.view.statsPanel.Model()
                 ]
                });
                
                //this.statsTable.get( "data" ).add( new Y.littleware.babyTrack.view.statsPanel.Model() );
                    
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
                                    return util.Date.minuteString( value );
                                } else {
                                    //log.fine( "Chart label ignoring date: " + dateMinuteString( value ) );
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
                
                //var viewportWidth  = document.documentElement.clientWidth
                //     , viewportHeight = document.documentElement.clientHeight;

                //Y.on( 'windowresize', function(ev){} ); // bla
                
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
                    var nowMinus20 = new Date( now.getTime() - 20*60*1000 );
                    this.startTime = new Date( Math.floor( nowMinus20 / msInMin) * msInMin );
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

                var viewportWidth  = document.documentElement.clientWidth
                     , viewportHeight = document.documentElement.clientHeight;

                var chartDiv = root.one( "figure" );
                chartDiv.setHTML( "" );
                if( viewportWidth > 400 && viewportHeight > 400 ) {
                    // do not render chart on small (phone) screens
                    this.chart.render( chartDiv );
                } else {
                    // probably on a phone ...
                    chartDiv.hide();
                    // scroll to hide the browser URL chrome ...
                    //   http://localhost:8080/btrack/babyTrack/en/511.html
                    Y.one( "body" ).setStyle( "min-height", "480px" );
                    setTimeout(function(){
                        window.scrollTo( window.pageXOffset,0);
                        }, 0);
                }
                
                var statsDiv = root.one( "div#stats" );
                statsDiv.setHTML( "" );
                this.statsTable.render( statsDiv );
                
                var buttonContainer = root.one( "div#button" );
                log.fine( "render() Clearing button container ..." );
                buttonContainer.setHTML( "" );
                this.buttonPanel.set( "container", buttonContainer  );
                this.buttonPanel.render();
                
                
                if ( Y.one( "#clock" ) ) {
                    var tickTock = function() {
                            var clockNode = Y.one( "#clock" );
                            if ( clockNode ) {
                                clockNode.setHTML( util.Date.secondString( new Date() ));
                            }
                        };
                    tickTock();
                    this.clockInterval = window.setInterval( 
                        tickTock, 1000
                    );
                }
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
                buttonModel.after( "change", function(ev) {
                        // sync app-level model with button-panel model
                        view.controller.toggleContraction( ev );
                } );
                this.controller.loadState();
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
                   this.view.statsTable.getRecord(0).updateStats( this.model );
             }

               // this is a goofy place to do this ...
             this.view.chart.set( "dataProvider", this.view.buildChartModel() );
               //this.view.chart.render();
             this.saveState();
          }

        
        var Suitcase = function() {
            return {
                "history":[],
                "currentStart":null
            };
        }
        
        /**
         * Save the contractionPanel model state to local storage
         * @method saveState
         * @return the json structure saved to local storage
         */
        Controller.prototype.saveState = function() {
            var suitcase = new Suitcase();
            this.model.get( "history" ).each( function( sample ) {
                var copy = {
                  startTime: sample.get( "startTime" ).getTime(),
                  endTime: sample.get( "endTime" ).getTime()
                };
                suitcase.history.push( copy );
            });
            if ( (this.model.get( "state" ) == "in") && this.model.builder ) {
                suitcase.currentStart = this.model.builder.startTime.getTime();
            }
            localStorage.suitcase = JSON.stringify( suitcase );
            return suitcase;
        }
        
        /**
         * Load the contractionPanel state from local storage.
         * Only works if run once at beginnning of app before 
         * first render() - otherwise
         * things get weird.
         * @method loadState
         * @return the json structure loaded from local stroage and
         *             applied to the contractionPanel models
         */
        Controller.prototype.loadState = function() {
            var suitcase = new Suitcase();
            if ( localStorage.suitcase ) {
                suitcase = JSON.parse( localStorage.suitcase );
            }
            
            // load samples from local storage,
            // filter out data older than 2 hours
            var minus2Hours = new Date(
                (new Date()).getTime() - 2*60*60*1000
                );
            var minus24Hours = new Date(
                (new Date()).getTime() - 24*60*60*1000
                );
                    
            this.model.get( "history" ).reset( 
              Y.Array.filter(
                    Y.Array.map( suitcase.history, function( sample ) {
                      return model.contraction.Factory.get(
                         ).withStartTime( new Date( sample.startTime ) 
                         ).withEndTime( new Date( sample.endTime )
                         ).build()
                  }),
                  function( sample ) {
                      return sample.startTime.getTime() > minus24Hours.getTime();
                  }
              )
            );

            this.view.chart.set( "dataProvider", this.view.buildChartModel() );
            this.view.statsTable.getRecord(0).updateStats( this.model );
            
            // restore "in-contraction" state if necessary
            if ( suitcase.currentStart && (suitcase.currentStart > minus2Hours.getTime()) ) {
                var startTime = new Date( suitcase.currentStart );
                log.fine( "loadState setting app model in startContraction state ..." );
                this.model.startContraction( startTime );
                var buttonModel = this.view.buttonPanel.get( "model" ); 
                // update button model silently,
                // so it doesn't trigger changes in app-level listeners
                log.fine( "loadState() Updating button model ..." );
                buttonModel.setAttrs( {
                   state:"running",
                   startTime: startTime,
                   endTime:null
                }, { silent:true });
                // 1st call to render should do this: this.view.buttonPanel.syncState();
            }
            log.info( "Done loading state ..." );
            return suitcase;
        }
        
        /**
         * Clear the application state 
         */
        Controller.prototype.clearState = function() {
            delete( localStorage.suitcase );
            this.loadState();
        }
        
        //----------------------------------
        
        return {
            Factory:Factory
        };
    }
  )();
}, '0.1.1' /* module version */, {
    requires: [ 'base', 'charts', 'datatable', 
        'littleware-babyTrack-model-contraction', 
        'littleware-babyTrack-model-contractionApp', 
        'littleware-babyTrack-view-startStopButtonPanel', 
        'littleware-babyTrack-view-statsPanel', 
        'littleware-littleUtil', "model-list", 'view' ]
});



