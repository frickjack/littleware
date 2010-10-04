/*
 * Copyright 2010, catdogboy@yahoo.com
 * General use subject to GPLv2: http://www.gnu.org/licenses/gpl-2.0.html
 */

YUI.namespace( "littleware.browser" )
littleware = YUI.littleware


/**
 * Model module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 *
 * @module Model
 * @namespace littleware.browser
 */
littleware.browser.Model = function() {
    var ModelBuilder = function() {
        this.asset = null;
        this.children = new Array();
        this.uncles = new Array();
        this.siblings = new Array();
        return this;
    }

    ModelBuilder.prototype.addChild = function( asset ) {

    };

    return {
      ModelBuilder: ModelBuilder
    };
}();

