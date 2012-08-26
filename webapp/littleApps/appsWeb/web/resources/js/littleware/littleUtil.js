/*
 * Copyright 2011 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * littleware.littleUtil module,
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.littleUtil
 * @namespace littleware.littleUtil
 */
YUI.add('littleware-littleUtil', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleUtil = (function() {
        
        //------------------------------------
        
        /**
         * The Logger class just calls through to Y.log with the
         * constructor-supplied logger-name
         * 
         * @class Logger
         * @constructor
         * @param loggerName string
         */        
        function Logger( loggerName ) {
            this.loggerName = loggerName;
        }

        /**
         * Call through to Y.log
         * 
         * @param msg string
         * @param level string - optional
         */
        Logger.prototype.log = function ( msg, level ) {
                if( Y.Lang.isUndefined( level ) ) {
                    level = 'info';
                }
                Y.log( msg, level, this.loggerName );
            };
            
        /**
         * Return the keys of the given object
         * @method keys
         * @param o object to scan for keys
         * @return {Array} of key-names
         * @for
         * @static
         */
        function keys( o ) {
            var keys = [];
            for( var k in o ) keys.push( k );
            return keys;
        }
        
        /**
         * Throws new Error( message ) if predicate evaluates false
         * @method assert
         * @param {Boolean} predicate
         * @param {String} message
         */
        function assert( predicate, message ) {
            if( ! predicate ) {
                throw new Error( message );
            }
        }
        
        /**
         * Return a test suite to test this submodule
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-littleUtil Test Suite");
            suite.add( new Y.Test.Case( {
                name: "Bogus Test",
                testBogus: function() {
                    Y.Assert.isTrue( true, "Do nothing test" );
                }
            }
            ));

            return suite;
        };
        // expose an api
        return {
            buildTestSuite: buildTestSuite,
            Logger:Logger,
            keys:keys,
            assert:assert
        };
    })();
}, '0.1.1' /* module version */, {
    requires: []
});
