/*
 * Copyright 2011 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * littleware.littleId module,
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
        * Post a message to the message queue
        * 
        * @class littleMessage
        * @static
        * @method postMessage
        * @param messageType {String} of content
        * @param payloadType {String} of content
        * @param content message payload content
        * @param callback passed list of responses - maybe be invoked
        *          multiple times as partial responses are retrieved from the server
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
            Y.io( uriBase + "/handle/" + now, {
                method: "PUT",
                data: JSON.stringify( message ),
                on: {
                    complete: function(id, ev) {
                        log.log( "response: " + ev.responseText );
                        var json = JSON.parse(ev.responseText);
                        callback( json );
                    }
                }
            });
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
                            function( response ) {
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

