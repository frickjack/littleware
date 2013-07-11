/*
 * Copyright 2011 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

if( typeof( littleware ) == 'undefined' ) {
    littleware = {};
} 


/**
 * littleYUI module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 * Provides convenience method for loading YUI with the littleware extension modules,
 * so client javascript code just invokes:
 *      littleware.littleYUI.bootstrap()... 
 *
 * @module littleware.littleYUI
 * @namespace auburn.library
 */
littleware.littleYUI = (function() {
    var config = {
        CONTEXT_ROOT : "/littleware_apps"
    };
    
    /**
     * Get the YUI.config groups entry that registers the littleware javascript
     * modules with - YUI( { ..., groups: { littleware: getLittleModules(), ... } )
     * @method getLittleModules
     * @return dictionary ready to add to YUI config's groups dictionary
     */
    var getLittleModules = function() {
        return {
            combine: false,
            base: config.CONTEXT_ROOT + '/resources/js/littleware/',
            //comboBase: 'http://yui.yahooapis.com/combo?',
            //root: '2.8.0r4/build/',
            modules:  { // one or more external modules that can be loaded along side of YUI
                'littleware-littleUtil': {
                    path: "littleUtil.js",
                    requires: [ "array-extras", "handlebars", "io", "node", "promise", "querystring-stringify-simple", 'test' ]
                },
                'littleware-littleId': {
                    path: "littleId.js",
                    requires: ['anim', 'base', 'node-base', 'node']
                },
                'littleware-littleTree': {
                    path: "littleTree.js",
                    requires: ['anim', 'base', 'node', 'node-base', 'test']
                },
                'littleware-littleMessage': {
                    path: "littleMessage.js",
                    requires: [ 'io-base', 'node', 'node-base', 'littleware-littleUtil', 'test']
                },
                'littleware-feedback-model': {
                    path: "feedback/littleFeedback.js",
                    requires: [ 'node', 'base', 'littleware-littleUtil', 'test']
                },
                'littleware-feedback-view': {
                    path: "feedback/FbWidget.js",
                    requires: [ 'node', 'base', 'littleware-littleUtil', 'littleware-feedback-model', 'test']
                },
                'littleware-toy-toyA': {
                    path: "toy/toyA.js",
                    requires: [ 'node', 'base', 'littleware-littleUtil', 'test']
                },
                'littleware-toy-toyB': {
                    path: "toy/toyB.js",
                    requires: [ 'littleware-toy-toyA']
                },
                "littleware-eventTrack-littleApp": {
                    path: "eventTrack/littleApp.js",
                    requires: [ 'event-gestures', 'littleware-littleUtil', 'router', 
                                'test', 'timers', 'view', 'transition' 
                            ]
                }
            }
        }    ;
    };

    /** Little internal merge function ... */
    function merge( a, b ) {
        var c = {};
        for ( var key in a ) {
            c[key] = a[key];
        }
        for ( var key in b ) {
            c[key] = b[key];
        }
        return c;
    }
    
    /**
     * littleYUI - wrapper around YUI3 YUI() method that
     * registers local littleware modules.
     * 
     * @param configIn YUI(config) configuration - extended by bootstrap
     */
    var bootstrap = function ( configIn ) {
        var bootConfig = merge( configIn || {},
            {
                //lang: 'ko-KR,en-GB,zh-Hant-TW', // languages in order of preference
                //base: '../../build/', // the base path to the YUI install.  Usually not needed because the default is the same base path as the yui.js include file
                //charset: 'utf-8', // specify a charset for inserted nodes, default is utf-8
                //loadOptional: true, // automatically load optional dependencies, default false
                //combine: true, // use the Yahoo! CDN combo service for YUI resources, default is true unless 'base' has been changed
                filter: 'raw', // apply a filter to load the raw or debug version of YUI files
                timeout: 10000, // specify the amount of time to wait for a node to finish loading before aborting
                insertBefore: 'yuiInsertBeforeMe', // The insertion point for new nodes

                // one or more groups of modules which share the same base path and
                // combo service specification.
                groups: {
                    // Note, while this is a valid way to load YUI2, 3.1.0 has intrinsic
                    // YUI 2 loading built in.  See the examples to learn how to use
                    // this feature.
                    littleware: getLittleModules()
                }
            }
            );
        return YUI( bootConfig );
    };
    return {
        config: config,
        bootstrap: bootstrap,
        getLittleModules: getLittleModules
    };
})();


/**
 * AMB module hook - still needs work ...
 * @param argNames {Array[String]}
 * @param moduleThunk {function(requires,exports,import1, import2, ...)}
 */
function define( argNames, moduleThunk ) {
  var name = null;
  // hacky way to get the YUI module name ...
  try { moduleThunk( null, null ); } catch ( v ) { name = v; }
  YUI.add(name, function(Y) {
    var thunkArgs = [];
    for( var i=0; i < argNames.length; ++i ) {
      thunkArgs.push( Y );
    }
    moduleThunk.apply( Y, thunkArgs );
  }, '0.1.1' );
}