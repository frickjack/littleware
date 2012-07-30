/*
 * Copyright 2012 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * littleware.littleFeedback module manages a strip of
 * UI feedback boxes.  Typical use:  
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
YUI.add( 'littleware-littleFeedback', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleFeedback = (function() {
        var log = new Y.littleware.littleUtil.Logger( "littleFeedback" ); 
        
        /**
         * Class tracks the state of a task, and updates
         * a task view.  Exports
         * properties that views can listen on.
         *
         * @param config must include srcNode within which to render the UI,
         *                 and optionally a 'description'
         * @class FeedbackModel
         * @constructor
         */
        function FeedbackModel( config ) {
            FeedbackModel.superclass.constructor.apply( this, [config] );
            this.isRendered = false;
            this.description = "";
            
            return this;
        }
        
        FeedbackModel.NAME = "FeedbackModel";
        
        FeedbackModel.ATTRS = {
            description: {
                value: "",
                readOnly:true
            },
            
            /**
             * Queue of user messages 
             * 
             * @attribute messageQueue
             * @readOnly
             * @type Array
             */
            messageQueue: {
                value:[],
                readOnly:true
            }, 
            /**
             * Double clamped between 0 and 100 set via progress() method
             * 
             * @attribute progress
             * @readOnly
             * @type Double
             */
            progress: {
                value:0,
                readOnly:true
            }
        };
        
        Y.extend(FeedbackModel, Y.Base, { 
                /**
                * Chainable shortcut for set( "progress", value )
                * 
                * @method progress
                * @param val to set progress to, clamped to (0,100)
                * @return this
                */
                progress: function(val) {
                    if( val < 0 ) { val = 0; } else if ( val > 100 ) { val = 100; }
                    this.set( "progress", val );
                    return this;
                },
                
                /**
                 * Chainable appends message to message queue,
                 * and discards old messages when queue grows beyond 10.
                 */
                message: function( val ) {
                    var q = this.get( "messageQueue" );
                    q.push( val );
                    if ( q.length > 10 ) {
                        q = q.slice( q.length - 10 );
                    }
                    this.set( "messageQueue", q );
                    return this;
                }
                
            } );
            
        
        //-----------------------------------------------
        
        /**
         * Handle that task-launching client gets handed.
         * Currently just has context property injected.
         * The UX model is - task-launcher launches
         * task-lambda via TaskFactory.
         * 
         * @class Task
         * @param context user context
         * @param feedback [FeedbackModel]
         * @constructor
         */
        function Task( context, feedback ) {
            Task.superclass.constructor.apply( this, [] );
            this.context = context;
            this.feedback = feedback;
            this.result = undefined;
            return this;
        }
        
        Task.NAME = "Task";
        
        Task.ATTRS = {
            /**
             * State of Task: PENDING, RUNNING, COMPLETE, EXCEPTION
             * 
             * @attribute state
             * @readOnly
             * @type Array
             */
            state: {
                value:"PENDING",
                readOnly:true
            },
            
            /**
             * Result initially undefined, Task.run() sets this to the lambda
             * result or the exception thrown by the lambda.
             */
            result: {
                value: undefined,
                readOnly:true
            }
        }
        
        Y.extend(Task, Y.Base, { 
            /**
             * Run the given function lambda: set state to RUNNING, run the lambda,
             * set result, set state to COMPLETE or EXCEPTION
             *
             * @method run
             * @param lambda called with arguments passed to constructor: lambda( feedback, context )
             * @return lambda result or rethrow exception thrown by lambda
             */
            run: function( lambda ) {
               var state = this.get( "state" );
               if( state != "PENDING" ) {
                   throw new Error( "Task not in PENDING state: " + state );
                   // throw new IllegalArgumentException bla bla
               } 
               try {
                   this.set( "state", "RUNNING" );
                   var result = lambda( this.feedback, this.context );
                   this.set( "result", result );
                   this.set( "state", "COMPLETE" );
                   return result;
               } catch(ex) {
                   this.set( "result", ex );
                   this.set( "state", "EXCEPTION" );
                   throw ex;
               }
            }
                }
            );
        
        //-----------------------------------------------
        
        /**
         * TaskFactory allows users to create tasks that interact
         * with the UI via a FeedbackModel
         * 
         * @class TaskFactory
         * @constructor
         */
        function TaskFactory() {
            TaskFactory.superclass.constructor.apply( this, [] );
            this.activeTasks = [];
            this.recentlyCompletedTasks = [];
            return this;
        }
        
        TaskFactory.ATTRS = {
            activeTasks: {
                value: [],
                readOnly:true
            },
            recentlyCompletedTasks: {
                value:[],
                readOnly:true
            }            
        }
        
        Y.extend(TaskFactory, Y.Base, { 
               /**
                * Setup a new task, and add it to the activeTasks.
                * Returns a Task object with context and feedback properties
                * that the caller can pass to the lambda:
                * <pre>
                *     var task = tf.pushTask( {}, "demo task" );
                *     ...
                *     task.run( function { 
                *        lambda( task.context, task.feedback, ... )  // or whatever
                *        } );
                * </pre>
                * The Task.run() method properly maintains the Task's run state,
                * so that UI observers can update accordingly.
                * 
                * @method newTask
                * @param userContext client object to set TaskContext.userContext
                * @param description of task
                * @return [Task] a new Task with a context property
                *              that the caller can pass to the task-lambda
                */
                pushTask: function( userContext, description ) {
                    var task = new Task( new FeedbackModel( { description: description } ), userContext )
                    var taskStack = this.get( "activeTasks" )
                    taskStack.push( task );
                    this._set( "activeTasks", taskStack );
                    
                    task.after( "stateChange", function( ev) {
                        if ( (ev.newVal == "COMPLETE") || (ev.newVal == "EXCEPTION") ) {
                            var taskStack = this.get( "activeTasks" )
                            var index = taskStack.indexOf( task );
                            if ( index > -1 ) {
                                taskStack.splice( index, 1 );
                                this._set( "activeTasks", taskStack );
                            }
                            var recentStack = this.get( "recentlyCompletedTasks" );
                            recentStack.push( task );
                            if ( recentStack.length > 10 ) {
                                recentStack.splice( 0, 10 - recentStack.length );
                            }
                            this._set( "recentlyCompletedTasks", recentStack );
                        }
                    });
                    
                    return task;
                }
                
            }
        );
        
        TaskFactory.singleton = new TaskFactory();
        
        //-----------------------------------------------
        
        /**
         * Return a test suite to test this submodule.
         * Test suite expects the hosting page to supply a
         * div with id #testSuiteTree to build a test tree in,
         * and a FeedbackViewTemplate script block of type text/x-template
         * or similar
         * 
         * @class littleFeedback
         * @static
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-littleFeedback Test Suite");
            suite.add( new Y.Test.Case( {
                name: "littleFeedback Test Case",
                testRunTask: function() {
                    var gotResponse = false
                    var taskFactoryEventCount = 0;
                    var taskFactory = TaskFactory.singleton;
                    taskFactory.after( "activeTasksChange", function(ev) {
                       taskFactoryEventCount += 1; 
                    });
                    var task = taskFactory.pushTask( {}, "test task" );
                    postMessage( "littleware.TestMessage", "littleware.apps.message.test.TestPayload", {
                                message : "javascript test case!"
                            },
                            function( session, vResponse ) {
                                gotResponse = true;
                            }
                        );
                    this.wait(function(){
                            Y.Assert.isTrue(gotResponse, "Got response from server");
                        }, 1000
                    );
                }
            }
            ));
            return suite;
        };

        //---------------------------------------
        
        return {
            buildTestSuite: buildTestSuite,
            postMessage: postMessage
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'io-base', 'node', 'node-base', 'littleware-littleUtil', 'test']
});

