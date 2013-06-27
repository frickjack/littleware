/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


YUI.namespace( "littleware.babyTrack" );


/**
 * littleYUI module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 * Provides convenience method for loading YUI with the littleware extension modules,
 * so client javascript code just invokes:
 *      littleware.littleYUI.bootstrap()... 
 *
 * @module littleware.littleYUI
 * @namespace littleware.littleYUI
 */
YUI.littleware.babyTrack.contractionMain = (function() {

    var CONTEXT_ROOT = "/btrack";
    
    /*
     * The YUI.config groups entry that registers the littleware javascript
     * modules with - YUI( { ..., groups: { littleware: getLittleModules(), ... } )
     */
    var littleModules = {
            combine: false,
            base: CONTEXT_ROOT + '/resources/js/littleware/',
            //comboBase: 'http://yui.yahooapis.com/combo?',
            //root: '2.8.0r4/build/',
            modules:  { // one or more external modules that can be loaded along side of YUI
                'littleware-littleUtil': {
                    path: "littleUtil.js",
                    requires: [ "array-extras" ]
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
                'littleware-babyTrack-model-contraction': {
                    path: "babyTrack/model/contraction.js",
                    requires: [ 'littleware-littleUtil' ]
                },
                'littleware-babyTrack-model-contractionApp': {
                    path: "babyTrack/model/contractionApp.js",
                    requires: [ 'base', 'event', 'littleware-babyTrack-model-contraction', 'model', 'model-list', 'node' ]
                },                
                'littleware-babyTrack-view-contractionPanel': {
                    path: "babyTrack/view/contractionPanel.js",
                    requires: [ 'base', 'charts', 'datatable', 
                        'littleware-babyTrack-model-contraction', 
                        'littleware-babyTrack-model-contractionApp', 
                        'littleware-babyTrack-view-startStopButtonPanel',
                        'littleware-babyTrack-view-statsPanel',
                        'littleware-littleUtil', "model-list", 'view' ]
                },
                'littleware-babyTrack-view-startStopButtonPanel': {
                    path: "babyTrack/view/startStopButtonPanel.js",
                    requires: [ 'base', 'littleware-littleUtil', "model", 'view' ]
                },
                'littleware-babyTrack-view-statsPanel': {
                    path: "babyTrack/view/statsPanel.js",
                    requires: [ 'base', 'littleware-littleUtil', "model", 'view' ]
                }                
            }
        };

    
    /**
     * littleYUI - wrapper around YUI3 YUI() method that
     * registers local littleware modules.
     */
    var bootstrap = function () {
        return YUI({
            //lang: 'ko-KR,en-GB,zh-Hant-TW', // languages in order of preference
            //base: '../../build/', // the base path to the YUI install.  Usually not needed because the default is the same base path as the yui.js include file
            //charset: 'utf-8', // specify a charset for inserted nodes, default is utf-8
            //loadOptional: true, // automatically load optional dependencies, default false
            //combine: true, // use the Yahoo! CDN combo service for YUI resources, default is true unless 'base' has been changed
            filter: 'raw', // apply a filter to load the raw or debug version of YUI files
            timeout: 10000, // specify the amount of time to wait for a node to finish loading before aborting
            insertBefore: 'yuiInsertBeforeMe', // The insertion point for new nodes
            debug:true,
            // one or more groups of modules which share the same base path and
            // combo service specification.
            groups: {
                // Note, while this is a valid way to load YUI2, 3.1.0 has intrinsic
                // YUI 2 loading built in.  See the examples to learn how to use
                // this feature.
                littleware: littleModules
            }
        });
    };
    
    /**
     * Main entry point for contractions application.
     * Sets up YUI context, and registers event handlers, etc.
     * 
     * @param {function} lambda optional thunk called with lambda(Y)
     */
    var main = function( lambda ) {
        bootstrap().use( 
                'node', 'node-base', 'event', 'test', 'scrollview',
                'littleware-babyTrack-view-contractionPanel',
                function (Y) {
                    var util = Y.littleware.littleUtil;
                    var log = new util.Logger( "btrack-contractionMain" );
                    
                    log.log( "main() running" );
                    var view = Y.littleware.babyTrack.view.contractionPanel.Factory.get().build();
                    view.render();
                    if ( lambda ) {
                        lambda( Y );
                    }
                }
        );
    };
    
                    
    return {
        bootstrap: bootstrap,
        CONTEXT_ROOT:CONTEXT_ROOT,
        main:main
    };
})();

