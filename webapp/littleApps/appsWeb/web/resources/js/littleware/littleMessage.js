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
        * @param content object stringified to JSON for submission
        * @param callback passed list of responses - maybe be invoked
        *          multiple times as partial responses are retrieved from the server
        */
        function postMessage( messageType, payloadType, content, callback ) {
            var data = content
            var now = new Date().getTime()
            if ( ! Y.Lang.isString( content ) ) {
                data = JSON.stringify( content )
            }
            Y.io( uriBase + "/handle/" + now, {
                method: "PUT",
                data: data,
                on: {
                    complete: function(id, ev) {
                        var json = JSON.parse(ev.responseText);
                        console.log(json);
                    }
                }
            });
        }

        /**
         * Check for responses to previously posted message
         * 
         * @private
         * @param messageId {String} of posted message
         
         */
        function checkResponse( messageId, callback ) {
            
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
                    var result = "bla"
                    Y.Assert.isTrue( result == "<b>Test!</b><br /><p>Super Bad!</p>", "Got expected template result: " + result  );
                }
            }
            ));
            return suite;
        };

        //---------------------------------------
        
        return {
            buildTestSuite: buildTestSuite,
            postMessage: postMessage,
            checkResponse: checkResponse
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'base', 'io-base', 'node', 'node-base', 'littleware-littleUtil']
});

