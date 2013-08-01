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
 * @module littleware-littleUtil
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
         * level property limits log messages - 
         * only logs messages with category >= level
         * where 'debug' == 1 and 'info' == 2.
         * Default level is 0.
         * @static
         */
        Logger.level = 0;

        /**
         * Call through to Y.log
         * 
         * @param msg string
         * @param cat string - optional
         */
        Logger.prototype.log = function ( msg, cat ) {
                var level = 2;
                if( Y.Lang.isUndefined( cat ) ) {
                    level = 2;
                } else if ( cat == 'fine' ) {
                    level = 1;
                }
                if ( level > Logger.level ) {
                    Y.log( msg, cat, this.loggerName );
                }
            };
            
        /** 
         * Shortcut for log( msg, 'info' )
         */
        Logger.prototype.info = function( msg ) {
            this.log( msg, 'info' );
        };
        /**
         * Shortcut for log( msg, 'fine' )
         */
        Logger.prototype.fine = function( msg ) {
            this.log( msg, 'fine' );
        };
        
        var log = new Logger( "littleUtil" );
            
        /**
         * Return the keys of the given object
         * @method keys
         * @param o object to scan for keys
         * @return {Array} of key-names
         * @for Module
         * @static
         */
        function keys( o ) {
            var keys = [];
            for( var k in o ) keys.push( k );
            return keys;
        }
        
        /**
         * Throws new Error( message ) if predicate evaluates false
         * 
         * @method assert
         * @param {Boolean} predicate
         * @param {String} message
         * @for Module
         * @static
         */
        function assert( predicate, message ) {
            if( ! predicate ) {
                throw new Error( message );
            }
        }
        
        
        /**
         * Little helper loads and compiles a handlebar template via an AJAX call
         * 
         * @method loadHandlebar
         * @param url {string}
         * @return {Promise} Y.Promise delivers compiled template function or error info
         * @static
         */
        function loadHandlebar( url ) {            
            var promise = new Y.Promise( function(resolve,reject) {
                var ioconf = {
                  method:"GET",
                  on: {
                    success:function( id, resp, args ){
                        resolve( Y.Handlebars.compile( resp.responseText ) );
                    },
                    failure:function( id, resp, args ) {
                        log.log( "Failed to load " + url + ": " + resp.status + ", " + resp.statusText );
                        reject( resp );
                    }
                  },
                  timeout:60000
                };
                Y.io( url, ioconf );
            });
            return promise;
        }
        
        //--------------------------------------
        
        /**
         * Helper for assembling Builder classes.
         * 
         * @namespace littleware.littleUtil
         * @class BuilderBuilder
         */
        var BuilderBuilder = {
          /**
           * Utility assembles a Builder class
           */            
          create: function( propsIn ) {
            var props = Y.Array.unique( propsIn );
              
            var Builder = function() {
                Y.Array.each( props, function(key) {
                   this[key] = null; 
                });
            };
            
            Builder.props = props;
            
            Y.Array.each( props, function(key) {
                var funcName = "with" + key[0].toUpperCase() + key.substr(1);
                //log.fine( "Registering builder function: " + funcName );
                Builder.prototype[ funcName ] = function( value ) {
                    this[key] = value;
                    return this;
                };
            });

            /** Return any unset properties */
            Builder.prototype.validate = function() {
                return Y.Array.filter( Builder.props, function(key) { 
                    return this[key] == null;
                }, this );
            }
            
            return Builder;
          }  
        };


        /**
         * Pad a positive integer to at least length 2 with a zero.
         * Mostly useful for formatting time-strings (dateMinuteString, etc).
         * @method zPadInt
         * @param {int} i
         * @return {string} i padded to length >= 2 with zero prefix if necessary
         */
        var zPadInt = function( i ) {
            if( i < 10 ) {
                return "0" + i;
            }
            return "" + i;
        };
        
        /**
         * Format a date down to minute: HH:MI
         * @method dateMinuteString
         * @param {Date} date
         */
        var dateMinuteString = function( date ) {
            var hour = date.getHours() % 12;
            hour = hour || 12;
            var minute = date.getMinutes();
            return "" + hour + ":" + zPadInt( minute );
        }
        
        /**
         * Format date to second: HH:MI:SS
         * @method dateSecondString
         * @param {Date} date
         */
        var dateSecondString = function( date ) {
            var second = date.getSeconds();
            return dateMinuteString( date ) + ":" + zPadInt( second );
        }

        /**
         * Return a test suite to test this submodule
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.TestSuite( "littleware-littleUtil Test Suite");
            suite.add( new Y.Test.TestCase( {
                name: "Builder Test",
                
                testBuilder: function() {
                    Y.Assert.isTrue( true, "Do nothing test" );
                    log.info( "Assembling builder" );
                    var Builder = BuilderBuilder.create( ["a", "b", "c"]);
                    var builder = new Builder();
                    log.info( "Chaining builder assignment" );
                    builder.withA( "a" ).withB( "b" ).withC( "c" );
                    log.info( "Checking builder values" );
                    Y.Assert.isTrue( builder.a == "a" && builder.b == "b" && builder.c == "c", "BuilderBuilder sets up reasonable builder");
                    Y.Assert.isTrue( builder.validate().length == 0, "test builder validates ok" );
                },
                
                testLoadHandlebar: function() {
                    var test = this;
                    
                    var promise = loadHandlebar( littleware.littleYUI.config.CONTEXT_ROOT + "/resources/templates/testcase.handlebars" );
                    promise.then( function( template ) {
                            var node = Y.Node.create( template( {name:"Reuben"} ) );
                            test.resume( function() {
                                log.log( "Template node has type: " + typeof( node ) + ": " + node.getHTML() );
                                Y.Assert.isTrue( node.one( "p" ).getHTML() == "Hello, Reuben!" ); 
                            });
                        }, function( err ) {
                            log.log( "Test template load failed: " + err );
                            test.resume( function() {
                                Y.Assert.fail( "Failed to load template: " + err );
                            });
                        });
                    
                    this.wait( 2000 );
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
            assert:assert,
            BuilderBuilder:BuilderBuilder,
            zPadInt:zPadInt,
            Date:{
                minuteString:dateMinuteString,
                secondString:dateSecondString
            },
            loadHandlebar:loadHandlebar
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ "array-extras", "handlebars", "io", "node", "promise", "querystring-stringify-simple", 'test' ]
});
