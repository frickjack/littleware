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
 *   <li>var mgr = new Feedback( );
 *   <li>var fb = mgr.startTask( "description" );
 *   <li>fb.progress( 50 ).message( "bla bla bla" )
 *   <li>fb.endTask
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
         * Class tracks the state of a UI task, and updates
         * a task view.  Exports
         * properties that other views can listen on.
         *
         * @class FeedbackView
         * @constructor
         */
        function FeedbackView() {
            FeedbackModel.superclass.constructor.apply( this, [] );
            this.isRendered = false;
            return this;
        }
        
        FeedbackView.NAME = "FeedbackView";
        
        FeedbackView.ATTRS = {
            messageQueue: {
                value:[],
                readOnly:true
            }, 
            progress: {
                value:0,
                readOnly:false
            }
        };
        
        Y.extend(FeedbackView, Y.Base, { 
                /**
                * Chainable shortcut for set( "progress", value )
                * 
                * @method progress
                * @return this
                */
                progress: function(val) {
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
                },
                
                /**
                 * Follow YUI convention with widget render() method
                 */
                render: function() {
                    
                }
            } );
    
        
        //-----------------------------------------------
        
        /**
         * TaskFactory allows users to create tasks that interact
         * with the UI via a FeedbackView
         * 
         * @class TaskFactory
         * @param config includes div property with CSS selector for div to build task bar into
         */
        function TaskFactory( config ) {
            this.config = config;
            this.isRendered = false;
            return this;
        }
        
        /**
         * Setup a new task view in the UI
         * 
         * @return [FeedbackView] a new FeedbackView that presents progress and 
         *     message information to the UI
         */
        TaskFactory.prototype.newTask = function( description ) {
            
        }
        
        TaskFactory.prototype.redner = function() {
            
        }
        
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
                testPostAndResponse: function() {
                    var gotResponse = false
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

