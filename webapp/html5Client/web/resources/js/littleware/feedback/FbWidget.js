/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * littleware.feedback.view module provides HTML widgets that
 * view the feedback and task models in littleware.feedback.model.
 * Typical use:  
 * <ul>
 *   <li>var taskBar = new TaskBar();
 *   <li>var task = taskBar.newTask( {}, "run test" );
 *   <li>task.run( function( ctx, fb ) { fb.progress( 50 ).message( "bla bla bla" ) } )
 * </ul>
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.littleFeedback
 * @namespace littleware.littleFeedback
 */
YUI.add( 'littleware-feedback-view', function(Y) {
    Y.namespace('littleware.feedback');
    Y.littleware.feedback.view = (function() {
        var util = Y.littleware.littleUtil;
        var log = new util.Logger( "littleFeedbackView" );
        
        //---------------------------------------
        /**
         * Associate a taskFactory view with a DOM div.
         * Requires #taskViewTemplate script text/x-template block in DOM
         * to instantiate task-views with.
         * @class TaskBar
         * @constructor
         * @param config properties object - includes displayLimit property, default 10
         */
        function TaskBar( config ) {
            this.config = config;
            if ( Y.Lang.isUndefined( this.config.displayLimit ) ) {
                this.config.displayLimit = 10;
            }
            this.knownTasks = {};
            this.taskOrder = [];
            this.taskTemplate = Y.one("#taskViewTemplate").get( 'text' );
            return this;
        }

        /**
         * Internal method - append new feedback widget to task bar,
         * and add a listener
         * 
         * @method _renderTaskView
         * @param {Task} task
         * @return {FeedbackListener} where result.node is the node already added to DOM
         * @private
         */
        TaskBar.prototype._renderTaskView = function( task ) {
            var taskBar = Y.one( this.config.srcNode );
            var taskNode = Y.Node.create( this.taskTemplate );
            taskNode.generateID();
            taskBar.appendChild( taskNode );
            var listener = new FeedbackListener( task, taskNode );
            listener.updateView();
            return listener;
        }
        
        /**
         * Configure the taskBar in the DOM - follows the YUI widget pattern of
         * defering DOM update until render() call.
         * @method render
         */
        TaskBar.prototype.render = function() {
            var srcNode = Y.one( this.config.srcNode );
            if( Y.Lang.isUndefined( srcNode ) ) {
                throw new Error( "Invalid taskBar source node: " + this.config.srcNode );
            }
            var taskFactory = Y.littleware.feedback.model.taskFactory; 
            var activeTasks = taskFactory.get( "activeTasks" );
            for( var i=0; i < activeTasks.length; ++i ) {
                var task = activeTasks[i];
                var listener = this._renderTaskView( task );
                this.knownTasks[ task.id ] = listener;
            }
            taskFactory.after( "activeTasksChange", function(ev) {
                // add code to listen on the new task, detach when task complete
                var taskBarNode = Y.one( this.config.srcNode );
                var activeTasks = ev.newVal;
                log.log( "activeTasksChange event - total tasks: " + activeTasks.length );
                var visibleTasks = Y.Object.size( this.knownTasks );
                for( var i in this.knownTasks ) {
                    if ( visibleTasks < this.config.displayLimit ) {break;}
                    var listener = this.knownTasks[i];
                    var task = listener.task;
                    if( task.get( "state" ) == "COMPLETE" || task.get( "state" ) == "EXCEPTION" ) {
                        taskBarNode.removeChild( listener.view );
                        delete( this.knownTasks[i] );
                        visibleTasks--;
                    }
                }
                for( var i=0; i < activeTasks.length; ++i ) {
                    var task = activeTasks[i];
                    if( Y.Lang.isUndefined( this.knownTasks[ task.id ] ) ) {
                        var listener = this._renderTaskView( task );
                        this.knownTasks[ task.id ] = listener;
                    }
                }
               }, this
            );
        }
        
        
        //-----------------------------
        
        /**
         * Listener on changes to FeedbackModel updates view
         * @class FeedbackListener
         * @constructor
         * @param {Task} task to listen for attribute changes - must have task.feedback property
         * @param {Node} view to update
         */
        function FeedbackListener( task, view ) {
            util.assert( ! Y.Lang.isUndefined( task.feedback ), "Task must have feedback property to listen on" );
            this.task = task;
            this.view = view;
            this.subscription = [
                this.task.after( "stateChange", this.updateView, this ),
                this.task.feedback.after( "progressChange", this.updateView, this ),
                this.task.feedback.after( "messageQueueChange", this.updateView, this )
            ];
        }
        
        FeedbackListener.prototype.updateView = function() {
            var progressNode = this.view.one( "div.fbBar" );
            var progress = this.task.feedback.get( "progress" );
            progressNode.setContent( "(task" + this.task.id + ")" + progress + "%" );
            progressNode.setStyle( "width", "" + progress + "%" );
            
            var message = this.task.feedback.get( "description" ).replace( /</g, "&lt;" ) + "<br/> ----------------";
            var messageNode = this.view.one( "div.fbContent" );
            var mq = this.task.feedback.get( "messageQueue" );
            for( var i = mq.length > 2 ? mq.length - 2 : 0; i < mq.length; i++ ) {
                message += "<br/>" +  mq[i].replace( /</g, "&lt;" ); 
            } 
            messageNode.setContent( message  );
            if( this.task.get( "state" ) == "COMPLETE" || this.task.get( "state" ) == "EXCEPTION" ) {
                // detach listener ... UI gets cleaned up in TaskBar listener
                if( ! Y.Lang.isUndefined( this.subscription ) ) {
                    for( var i=0; i < this.subscription.length; ++i ) {
                        this.subscription[i].detach();    
                    }
                }
            }
        }
        
        
        //----------------------------------------
        
        /**
         * Return a test suite to test this submodule.
         * Test suite expects the hosting page to supply a
         * div with id #testSuite to build test UI in,
         * and a taskViewTemplate script block of type text/x-template
         * or similar
         * 
         * @class LittleTree
         * @static
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-FbWidget Test Suite");
            var taskBar = new TaskBar( {srcNode: "#testTaskBar"});
            taskBar.render();
            
            suite.add( new Y.Test.Case( {
                name: "littleware-feedback-view Test Case",
                testFeedbackAnimation: function() {
                    var result = "ugh";
                    var counter = 0;
                    var stopCount = taskBar.config.displayLimit + 5;
                    var task = Y.littleware.feedback.model.taskFactory.pushTask( {}, "FbWidget test task: " + counter );
                    
                    //var fbListener = taskBar._renderTaskView( task );
                    //Y.Assert.isNotUndefined( fbListener.view, "Listener has view property" );
                    var taskLambda = function() { 
                        var myTask = task;
                        if ( counter < stopCount - 1 ) {
                            task = Y.littleware.feedback.model.taskFactory.pushTask( {}, "FbWidget test task: " + counter );
                        } else {
                            task = null;
                        }
                        myTask.run( function(fb) {
                                        log.log( "Running test task" );
                                        fb.message( "Running task!" );
                                        log.log( "Run1" );
                                        fb.progress(100);
                                        log.log( "Run2" );
                                        fb.message( "Task complete!" );
                                        log.log( "Run3" );
                                    });
                    };
                    
                    var waitLambda = null;
                    lambda = function(){
                        //alert( "Hopefully the test suite taskBar updated for you ?" );
                        log.log( "Checking task run: " + counter );
                        var children = Y.one( taskBar.config.srcNode ).get( "children" ); // NodeList
                        Y.Assert.isTrue( children.size() > 0, "Test taskBar has some children, count: " + counter );
                        Y.Assert.isTrue( children.size() < taskBar.config.displayLimit, "Test taskBar respects displayLimit: " + taskBar.config.displayLimit );
                        counter++;
                        if ( counter < stopCount ) {
                            setTimeout( taskLambda, 100 );
                            this.wait( lambda, 1000 );
                        }
                    };
                    
                    setTimeout( taskLambda, 100 );
                    this.wait( lambda, 1000 );

                    //Y.Assert.isTrue( result == "<b>Test!</b><br /><p>Super Bad!</p>", "Got expected template result: " + result  );
                },
                testTaskBarUI: function() {
                }
            }
            ));
            return suite;
        };


        //-----------------------------------
        
        return {
            TaskBar:TaskBar,
            FeedbackListener:FeedbackListener,
            buildTestSuite:buildTestSuite
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'io-base', 'node', 'node-base', 
        'littleware-littleUtil', 'littleware-feedback-model', 
                'test']
});



