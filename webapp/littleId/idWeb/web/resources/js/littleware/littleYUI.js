/*
 * Copyright 2011 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

if( window.littleware == undefined ) {
    window.littleware = {};
}

/**
 * littleYUI module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.littleYUI
 * @namespace auburn.library
 */
littleware.littleYUI = (function() {

    /**
     * Get the YUI.config groups entry that registers the littleware javascript
     * modules with - YUI( { ..., groups: { littleware: getLittleModules(), ... } )
     * @method getLittleModules
     * @return dictionary ready to add to YUI config's groups dictionary
     */
    var getLittleModules = function() {
        return {
            combine: false,
            base: '/services/resources/js/littleware/',
            //comboBase: 'http://yui.yahooapis.com/combo?',
            //root: '2.8.0r4/build/',
            modules:  { // one or more external modules that can be loaded along side of YUI
                'littleware-littleUtil': {
                    path: "littleUtil.js"
                },
                'littleware-littleId': {
                    path: "littleId.js",
                    requires: ['anim', 'base', 'node-base', 'node']
                }
            }
        }    ;
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

            // one or more groups of modules which share the same base path and
            // combo service specification.
            groups: {
                // Note, while this is a valid way to load YUI2, 3.1.0 has intrinsic
                // YUI 2 loading built in.  See the examples to learn how to use
                // this feature.
                littleware: getLittleModules()
            }
        });
    };
    return {
        bootstrap: bootstrap,
        getLittleModules: getLittleModules
    };
})();
   


