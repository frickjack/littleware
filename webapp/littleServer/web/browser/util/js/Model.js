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
    
    /**
     * Model holds data for browser view
     * @class Model
     */
    var Model = function(builder) {
        this.asset = builder.asset;
        this.children = builder.children.slice();
        this.uncles = builder.uncles.slice();
        this.siblings = builder.siblings.slice();
        return this;
    };

    /**
     * Builder for Model behind browser UI view
     * @class ModelBuilder
     */
    var ModelBuilder = function() {
        this.asset = null;
        this.children = new Array();
        this.uncles = new Array();
        this.siblings = new Array();
        return this;
    }

    /**
     * Add a child asset to the view model
     * @method addChild
     * @return {ModelBuilder} this
     */
    ModelBuilder.prototype.addChild = function( asset ) {
        this.children.push( asset );
        return this;
    };

    /**
     * Read-only asset POJO that is basis of browser data model
     * @class Asset
     * @param builder {AssetBuilder}
     */
    var Asset = function( builder ) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        return this;
    };

    /**
     * Factory for Asset objects
     * @class AssetBuilder
     */
    var AssetBuilder = function() {
        this.name = "";
        this.id = "";
        this.type = "littleware.Generic";
        return this;
    };

    /**
     * Set the id property
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setId = function( value ) {
        this.id = value;
        return this;
    };

    /**
     * Set the name property
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setName = function( value ) {
        this.name = value;
        return this;
    };

    /**
     * Build an Asset with this builder's properties set
     * @return {Asset}
     */
    AssetBuilder.prototype.build = function() {
        return new Asset(this)
    };

    return {
      ModelBuilder: ModelBuilder
    };
}();

