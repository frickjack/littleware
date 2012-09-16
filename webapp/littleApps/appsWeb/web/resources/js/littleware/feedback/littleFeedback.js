/*
 * Copyright 2012 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * littleware.feedback.model implements models for tracking
 * tasks and feedback from task execution.
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
 * @module littleware.feedback.model
 * @namespace littleware.feedback.model
 */
YUI.add( 'littleware-feedback-model', function(Y) {
    Y.namespace('littleware.feedback');
    Y.littleware.feedback.model = (function() {
        var log = new Y.littleware.littleUtil.Logger( "littleFeedbackModel" ); 
        
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
            
            return this;
        }
        
        FeedbackModel.NAME = "FeedbackModel";
        
        FeedbackModel.ATTRS = {
            description: {
                value: "",
                initOnly:true
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
                    this._set( "progress", val );
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
                    this._set( "messageQueue", q );
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
         * @param feedback [FeedbackModel]
         * @param context user context
         * @constructor
         */
        function Task( feedback, context ) {
            Task.superclass.constructor.apply( this, [] );
            this.context = context;
            this.feedback = feedback;
            this.id = Task.idCounter;
            Task.idCounter += 1;
            return this;
        }
        
        Task.NAME = "Task";
        Task.idCounter = 0;
        
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
                   this._set( "state", "RUNNING" );
                   var result = lambda( this.feedback, this.context );
                   this._set( "result", result );
                   this._set( "state", "COMPLETE" );
                   return result;
               } catch(ex) {
                   log.log( "Task run() Caught exception: " + ex + ", " + ex.stack );
                   //console.trace();  // might be chrome specific ...
                   this._set( "result", ex );
                   this._set( "state", "EXCEPTION" );
                   throw ex;
               }
            },
            
            /**
             * Set the task to the RUNNING state.
             * Throws an Error if the task is not in the Pending or Running state.
             * Prefer run(lambda) for tasks that run synchronously.
             * 
             * @method setRunning
             */
            setRunning: function() {
               var state = this.get( "state" );
               if( state != "PENDING" && state != "RUNNING" ) {
                   throw new Error( "Task not in PENDING or RUNNING state: " + state );
               } 
               this._set( "state", "RUNNING" );
            },
            
            /**
             * Set the task to the COMPLETE state.
             * Prefer run(lambda) for tasks that run synchronously.
             * 
             * @method setComplete
             * @param result to associate with the completed task
             */
            setComplete:function( result ) {
                this._set( "result", result );
                this._set( "state", "COMPLETE" );
            },
            
            /**
             * Set the task to the EXCEPTION state
             * 
             * @method setException
             * @param ex exception result to associate with result
             */
            setException:function( ex ) {
                this._set( "result", ex );
                this._set( "state", "EXCEPTION" );
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
                    var task = 
                        new Task( new FeedbackModel( { description: description } ), userContext );
                    var taskStack = this.get( "activeTasks" );
                    taskStack.push( task );
                    this._set( "activeTasks", taskStack );
                    var sub = null;
                    sub = task.after( "stateChange", function( ev) {
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
                            sub.detach();
                        }
                    }, this );
                    
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
            //alert( "Foo!" );
            var suite = new Y.Test.Suite( "littleware-feedback-model Test Suite");
            suite.add( new Y.Test.Case( {
                name: "littleware-feedback-model Test Case",
                testFeedbackModels: function() {
                    //alert( "Frick!" );
                    log.log( "Running testFeedbackModels" );
                    var taskFactoryEventCount = 0;
                    var taskFactory = TaskFactory.singleton;
                    taskFactory.after( "activeTasksChange", function(ev) {
                       taskFactoryEventCount += 1; 
                    });
                    log.log( "Pushing test task" );
                    var task = taskFactory.pushTask( {}, "test task" );
                    Y.Assert.isTrue( taskFactoryEventCount == 1, "Task push updated active tasks" );
                    Y.Assert.isTrue( task.feedback.get( "description" ) == "test task", "Task description set correctly" );
                    log.log( "Got task ..." );
                    task.feedback.message( "test message" );
                    var messQ = task.feedback.get( "messageQueue" );
                    Y.Assert.isTrue( messQ.length == 1, "Feedback posts single message: " + messQ.length );
                    task.run( function(fb) {
                        log.log( "Running test task" );
                        fb.message( "Running task!" );
                        log.log( "Run1" );
                        fb.progress(100);
                        log.log( "Run2" );
                        fb.message( "Task complete!" );
                        log.log( "Run3" );
                    });
                    log.log( "Check 1" );
                    Y.Assert.isTrue( messQ.length == 3, "Feedback posts 2 more messages: " + messQ.length );
                    log.log( "Check 2" );
                    Y.Assert.isTrue( task.feedback.get( "progress" ) == 100, "Feedback progress is 100: " + task.feedback.get( "progress" ) );
                    log.log( "Check 3" );
                    Y.Assert.isTrue( taskFactoryEventCount == 2, "Task execution updated active tasks: " + taskFactoryEventCount );
                }
            }
            ));
            //alert( "Bla!" );
            return suite;
        };

        //---------------------------------------
        
        return {
            buildTestSuite: buildTestSuite,
            taskFactory: TaskFactory.singleton
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'node', 'base', 'littleware-littleUtil', 'test']
});

