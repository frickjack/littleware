/*
 * Copyright 2010, catdogboy@yahoo.com
 * General use subject to GPLv2: http://www.gnu.org/licenses/gpl-2.0.html
 */

YUI.add( "littleware.browser.View", function(Y) {
Y.namespace( "littleware.browser" )
var littleware = Y.littleware


/**
 * View module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 *
 * @module View
 * @namespace littleware.browser
 */
littleware.browser.View = function() {
    var config = {
        serverRoot : "/littleWeb"
    }
    var AssetOverViewTemplate = (function() {
        var container = new Node( document.createElement("div"))
        container.set( "id", "container" )
        var img = new Node( document.createElement( "img" ) )
        img.set( "alt", "nodeGraph" )
        img.set( "src", config.serverRoot + "/util/img/nodeGraph.png")
        img.set( "width", "100px")
        img.set( "style", "margin:20px;" )
        container.appendChild( img )
        var nameIconTable = new Node( document.createElement( "table" ) )
        nameIconTable.set( "class", "browsercell" )
        return container;
    })();
    

    var AssetOverView = function( asset ) {
        this.asset = asset;
    }

    AssetOverView.prototype.render = function() {

    }

    AssetOverView.prototype.renderTo = function( divId ) {

    }

    /**
     * @class ViewBuilder
     */
    var ViewBuilder = function() {
        /**
         * @property model {littleware.browser.Model.ModelBuilder}
         */
        this.model = null
        /**
         * @property divId {string} id of div to render view into
         */
        this.divId = "littleBrowser"
    }

    /**
     * Set the model property
     * @method model
     * @param value {littleware.browser.Model.Model}
     * @return {ViewBuilder} this
     */
    ViewBuilder.prototype.model = function( value ) {
        this.model = value
        return this
    }

    /**
     * Set the divId property
     * @method divId
     * @param value {string}
     * @return {ViewBuilder} this
     */
     ViewBuilder.prototype.divId = function( value ) {
         this.divId = value
         return this
     }
     
    ViewBuilder.prototype.render = function() {

    }
}();
}, "0.0" );

