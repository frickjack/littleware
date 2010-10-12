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
     * @param asset {Asset}
     * @return {ModelBuilder} this
     */
    ModelBuilder.prototype.addChild = function( asset ) {
        this.children.push( asset );
        return this;
    };

    /**
     * AssetLink
     * @class AssetLink
     */
    var AssetLink = function( id, type, xpath ) {
        this.id = id;
        this.type = type;
        this.xpath = xpath;
        var index = xpath.lastIndexOf( "/" );
        if ( (index < 0) || (index + 1 >= xpath.length) ) {
            throw "Illegal xpath: " + xpath;
        }
        this.name = xpath.substring( index + 1 );
        return this;
    }

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
        this.comment = "";
        this.lastUpdateDate = new Date();
        this.lastUpdater = "";
        this.updateComment = "";
        this.owner = "";
        this.acl = "";
        this.creator = "";
        this.createDate = new Date();
        this.state = 0;
        this.value = 0;
        this.home = "";
        this.toId = null;
        this.fromId = null;
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
     * Set the owner property - the name of the owner
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setOwner = function( value ) {
        this.owner = value;
        return this;
    }

    /**
     * Set the acl property - the name of the acl, or null if no acl
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setAcl = function( value ) {
        this.acl = value;
        return this;
    }

    /**
     * Set the name of the user that issued the last update
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setLastUpdater = function( value ) {
        this.lastUpdater = value;
        return this;
    }

    /**
     * Set the date of the last update
     * @param value {Date}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setLastUpdateDate = function( value ) {
        this.lastUpdateDate = value;
        return this;
    }

    /**
     * Set the comment associated with the last update
     * @param value {string
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setUpdateComment = function( value ) {
        this.updateComment = value;
        return this;
    }

    /**
     * Set the name of the creator
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setCreator = function( value ) {
        this.creator = value;
        return this;
    }

    /**
     * Set the create date
     * @param value {Date}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setCreateDate = function( value ) {
        this.createDate = value;
        return this;
    }

    /**
     * Set the asset state
     * @param value {int}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setState = function( value ) {
        this.state = value;
        return this;
    }

    /**
     * Set the asset's value property
     * @param value {float}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setValue = function( value ) {
        this.value = value;
        return this;
    };

    /**
     * Set the name of this asset's home
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setHome = function( value ) {
        this.home = value;
        return this;
    };

    /**
     * Set the asset's toId
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setToId = function( value ) {
        this.toId = value;
        return this;
    }

    /**
     * Set the asset's fromId
     * @param value {string}
     * @return {AssetBuilder} this
     */
    AssetBuilder.prototype.setFromId = function( value ) {
        this.fromId = value;
        return this;
    }

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

