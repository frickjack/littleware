/*
 * Copyright 2012 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.littleMessage
 * @namespace littleware.littleMessage
 */
YUI.add( 'littleware-littleMessage', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleMessage = (function() {
        var log = new Y.littleware.littleUtil.Logger( "littleMessage" ); 
        
        var uriBase = "/littleware/services/message"
        var creds = ""
        
        /**
         * Class tracks the state of a message posted to the server,
         * and the responses collected so far.
         * Has properties originalMessage, responseQ, and state.
         *
         * @class MessageSession
         * @constructor
         */
        function MessageSession( originalMessage ) {
            this.id = MessageSession.counter;
            MessageSession.counter += 1;
            this.originalMessage = originalMessage;
            this.state = "NEW";
            this.handle = "";
            this.responseQ = [];
            this.lastEvent = null;
            return this;
        }
        
        MessageSession.counter = 0;
        

        
       /**
        * Post a message to the message queue
        * 
        * @class littleMessage
        * @static
        * @method postMessage
        * @param messageType {String} of content
        * @param payloadType {String} of content
        * @param content message payload content
        * @param callback invoked multiple times as responses arrive,
        *       passed list of new responses, and the MessageSession tracking
        *       state so far
        */
        function postMessage( messageType, payloadType, content, callback ) {
            var now = new Date().getTime()
            var message = {
                messageType: messageType,
                payload: {
                    payloadType: payloadType,
                    content: content
                }                
            };
            var session = new MessageSession( message );
            Y.io( uriBase + "/handle/" + now, {
                method: "PUT",
                data: JSON.stringify( message ),
                on: {
                    complete: function(id, ev) {
                        log.log( "postMessage response: " + ev ); // JSON.stringify( ev ) );
                        session.lastEvent = ev;
                        if( ev.status == 200 ) {
                            var json = JSON.parse(ev.responseText);
                            if ( json.status == "ok" ) {
                                session.state = "RUNNING";
                                session.handle = json.handle;
                                //activeSessions[ session.id.toString() ] = session;
                                pollForResponse( session, callback );
                                callback( session, [] );
                            } else {
                                session.state = "EXCEPTION";
                                session.responseQ.push( json );
                                callback( session, [ json ] );
                            }
                        } else {
                            session.state = "ERROR";
                            callback( session, [] );
                        }
                    }
                }
            });
        }
        
        
        /**
         * Internal method polls for server response to previoiusly
         * submitted session.  Polls repeatedly until message enters
         * complete state.
         */
        function pollForResponse( session, callback ) {
            Y.io( uriBase + "/handle/" + session.handle, {
                method: "GET",
                on: {
                    complete: function(id, ev) {
                        log.log( "pollForResponse response: " + ev ); //JSON.stringify( ev ) );
                        session.lastEvent = ev;
                        if( ev.status == 200 ) {
                            var json = JSON.parse(ev.responseText);
                            if ( json.status == "ok" ) {
                                var lastIndex = json.envelopes.length - 1;
                                if( lastIndex >= 0 ) {
                                    session.state = json.envelopes[ lastIndex ].response.state;
                                }  
                                //session.handle = json.handle;
                                //activeSessions[ session.id.toString() ] = session;
                                if( (session.state != "COMPLETE") && (session.state != "FAILED") ) {
                                    // poll for next response
                                    pollForResponse( session, callback );    
                                } else {
                                    log.log( "Last response - not polling for another" );
                                }
                                callback( session, json.envelopes );
                            } else {
                                session.state = "AppException";
                                session.responseQ.push( json );
                                callback( session, [ json ] );
                            }
                        } else {
                            session.state = "Error";
                            callback( session, [] );
                        }                        
                    } 
                }
            } );
        }

        
        //-----------------------------------------------
        
        /**
         * Return a test suite to test this submodule.
         * Test suite expects the hosting page to supply a
         * div with id #testSuiteTree to build a test tree in,
         * and a treeNodeTemplate script block of type text/x-template
         * or similar
         * 
         * @class littleMessage
         * @static
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-littleMessage Test Suite");
            suite.add( new Y.Test.Case( {
                name: "littleMessage Test Case",
                testPostAndResponse: function() {
                    var gotResponse = false
                    postMessage( "littleware.TestMessage", "littleware.apps.message.test.TestPayload", {
                                message : "javascript test case!"
                            },
                            function( session, vResponse ) {
                                log.log( "Got response: " + JSON.stringify( vResponse, null, '  ' ) );
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

