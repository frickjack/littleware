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
        var log = new util.Logger( "littleTree" );
        
        //---------------------------------------
        /**
         * Associate a taskFactory view with a DOM div.
         * Requires #taskViewTemplate script text/x-template block in DOM
         * to instantiate task-views with.
         * @class TaskBar
         * @constructor
         * @param config properties object
         */
        function TaskBar( config ) {
            this.config = config;
            this.knownTasks = {};
            this.taskOrder = [];
            this.taskTemplate = Y.one("#taskViewTemplate").get( 'text' );
            return this;
        }

        /**
         * Internal method - append new feedback widget to task bar
         * @method drawTaskView
         * @param {Task} task
         * @return {Node} take node already added to DOM
         * @private
         */
        TaskBar.prototype.drawTaskView = function( task ) {
            var taskBar = Y.one( this.config.srcNode );
            var taskNode = Y.Node.create( this.taskTemplate );
            taskBar.appendChild( taskNode );
            return taskNode;
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
                var node = this.drawTaskView( task );
                var listener = new FeedbackListener( task, node );
            }
            taskFactory.after( "activeTasksChange", function(ev) {
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
            this.model.after( "stateChange", this.updateView, this );
        }
        
        FeedbackListener.prototype.updateView = function() {
            var progressNode = this.model.one( "div.fbBar" );
            var progress = this.task.get( "progress" );
            progressNode.setContent( progress.toString() );
            progressNode.setStyle( "width", "" + progress + "%" );
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
            suite.add( new Y.Test.Case( {
                name: "littleware-feedback-view Test Case",
                testFeedbackAnimation: function() {
                    var result = "ugh";
                    Y.Assert.isTrue( result == "<b>Test!</b><br /><p>Super Bad!</p>", "Got expected template result: " + result  );
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
    requires: [ 'io-base', 'node', 'node-base', 'littleware-littleUtil', 'littleware-feedback-model', 'test']
});



