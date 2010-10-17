/*
 * Copyright 2010, catdogboy@yahoo.com
 * General use subject to GPLv2: http://www.gnu.org/licenses/gpl-2.0.html
 */



/**
 * Model module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 *
 * @module Model
 * @namespace littleware.browser
 */
YUI.add( "littleware.browser.Model",  function(Y) {
    Y.namespace( "littleware.browser" )
    var littleware = Y.littleware;
    littleware.browser.Model = function() {
        /**
        * Little static utility class to manipulate paths
        */
        var PathUtil = {
            /**
             * @method getParent
             * @param path
             * @return {string} parent of given path
             */
            getParent: function( path ) {
                var lastSlash = path.lastIndexOf( "/" );
                if ( path.match( /[^\/]\/\.?\s*$/ )) {
                    lastSlash = path.substring( 0, lastSlash ).lastIndexOf( "/" );
                }
                if ( (lastSlash < 0) || (lastSlash + 1 >= path.length) ) {
                    throw "Illegal path: " + path;
                }
                if ( 0 == lastSlash ) {
                    return "/";
                }
                return path.substring( 0, lastSlash );
            },

            /**
             * Get the home associated with this path
             * @method getHome
             * @param path
             * @return {string} /home
             */
            getHome: function( path ) {
                var slash2 = path.indexOf( "/", 1 );
                if ( slash2 > 1 ) {
                    return path.substring( 0, slash2 );
                }
                return path;
            },

            /**
             * Get the base name of the given path
             * @method getName
             * @param path
             * @return {string} base name
             */
            getName: function( path ) {
                var lastSlash = path.lastIndexOf( "/" );
                if ( path.match( /[^\/]\/\.?\s*$/ )) {
                    path = path.substring( 0, lastSlash )
                    lastSlash = path.lastIndexOf( "/" );
                }

                if ( (lastSlash < 0) || (lastSlash + 1 >= path.length) ) {
                    throw "Illegal path: " + path;
                }
                var firstSlash = path.indexOf( "/" )
                if ( firstSlash != 0 ) {
                    throw "Illegal path - must start with /: " + path;
                }

                return path.substring( lastSlash + 1 );

            }
        }

        //...................................................

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

        //...................................................

        /**
         * AssetLink
         * @class AssetLink
         * @param id {string} uuid unique-id of the asset referenced by this link - never changes
         * @param type {string} type of the asset referenced - never changes
         * @param path {string} the current path to the asset with id - may fall out of sync as
         *                    updates are applied to the node database
         */
        var AssetLink = function( id, type, path ) {
            /**
             * @property id {string} uuid
             */
            this.id = id;
            /**
             * @property type {string}
             */
            this.type = type;
            /**
             * @property path {string} similar to xpath
             */
            this.path = path;
            /**
             * Base of the path property
             * @property name {string}
             */
            this.name = PathUtil.getName( path );
            return this;
        }

        /**
         * Test equality with another AssetLink
         * @method equals
         * @param other {AssetLink}
         * @return {boolean}
         */
        AssetLink.prototype.equals = function( other ) {
            return ((other.id == this.id) && (other.type == this.type));
        }

        //............................................................

        /**
         * Read-only asset POJO that is basis of browser data model
         * @class Asset
         * @extends AssetLink
         * @param builder {AssetBuilder}
         */
        var Asset = function( builder ) {
            /**
             * @property path {string}
             */
            this.path = builder.path;
            /**
             * @property path {string} uuid
             */
            this.id = builder.id;
            /**
             * @property type {string}
             */
            this.type = builder.type;
            /**
             * @property comment {string}
             */
            this.comment = builder.comment;
            /**
             * @property lastUpdateDate {Date}
             */
            this.lastUpdateDate = builder.lastUpdateDate;
            /**
             * @property createDate {Date}
             */
            this.createDate = builder.createDate;
            /**
             * @property updateComment {string}
             */
            this.updateComment = builder.updateComment;
            /**
             * @property state {int}
             */
            this.state = builder.state;
            /**
             * @property value {float}
             */
            this.value = builder.value;
            /**
             * @property links {associative array}
             */
            this.links = Y.merge( builder.links );
            return this;
        };

        Asset.prototype = new AssetLink( "", "", "/unknown");

        /**
         * @method getLink
         * @param name {string} link name
         * @return {AssetLink} may be null
         */
        Asset.prototype.getLink = function( name ) {
            return this.links[ name ];
        };
    
        /**
         * Same as getLink( "owner" )
         * @method getOwner
         * @return {AssetLink}
         */
        Asset.prototype.getOwner = function() {
            return this.getLink( "owner" );
        };
        /**
         * Same as getLink( "acl" )
         * @method getAcl
         * @return {AssetLink}
         */
        Asset.prototype.getAcl = function() {
            return this.getLink( "acl" );
        };
        /**
         * Same as getLink( "creator" )
         * @method getCreator
         * @return {AssetLink}
         */
        Asset.prototype.getCreator = function() {
            return this.getLink( "creator" );
        };
        /**
         * Same as getLink( "lastUpdater" )
         * @method getLastUpdater
         * @return {AssetLink}
         */
        Asset.prototype.getLastUpdater = function() {
            return this.getLink( "lastUpdater" );
        }

        /**
         * Same as getLink( "to" )
         * @method getTo
         * @return {AssetLink}
         */
        Asset.prototype.getTo = function() {
            return this.getLink( "to" );
        }

        /**
         * Same as getLink( "from" )
         * @method getFrom
         * @return {AssetLink}
         */
        Asset.prototype.getFrom = function() {
            return this.getLink( "from" );
        }
        /**
         * Same as getLink( "home" )
         * @method getHome
         * @return {AssetLink}
         */
        Asset.prototype.getHome = function() {
            return this.getLink( "home" );
        }

        // .........................................

        /**
         * Factory for Asset objects
         * @class AssetBuilder
         * @param Y {YUI}
         */
        var AssetBuilder = function(Y) {
            /**
             * @property path {string}
             */
            this.path = "";
            /**
             * @property path {string} uuid
             */
            this.id = "";
            /**
             * @property type {string}
             */
            this.type = "littleware.Generic";
            /**
             * @property comment {string}
             */
            this.comment = "";
            /**
             * @property lastUpdateDate {Date}
             */
            this.lastUpdateDate = new Date();
            /**
             * @property createDate {Date}
             */
            this.createDate = new Date();
            /**
             * @property updateComment {string}
             */
            this.updateComment = "";
            /**
             * @property state {int}
             */
            this.state = 0;
            /**
             * @property value {float}
             */
            this.value = 0;
            /**
             * @property links {associative array}
             */
            this.links = {};
            return this;
        };


        /**
         * Set the id property
         * @method setId
         * @param value {string}
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setId = function( value ) {
            this.id = value;
            return this;
        };

        /**
         * Set the path property - implicitly sets home and name of Asset at build time
         * @method setPath
         * @param value {string}
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setPath = function( value ) {
            this.path = value;
            return this;
        };

        /**
         * Set the comment property
         * @method setComment
         * @param value {string}
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setComment = function( value ) {
            this.comment = value;
            return this;
        };


        /**
         * Set the date of the last update
         * @method setLastUpdateDate
         * @param value {Date}
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setLastUpdateDate = function( value ) {
            this.lastUpdateDate = value;
            return this;
        }

        /**
         * Set the comment associated with the last update
         * @method setUpdateComment
         * @param value {string
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setUpdateComment = function( value ) {
            this.updateComment = value;
            return this;
        }


        /**
         * Set the create date
         * @method setCreateDate
         * @param value {Date}
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setCreateDate = function( value ) {
            this.createDate = value;
            return this;
        }

        /**
         * Set the asset state
         * @method setState
         * @param value {int}
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setState = function( value ) {
            this.state = value;
            return this;
        }

        /**
         * Set the asset's value property
         * @method setValue
         * @param value {float}
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setValue = function( value ) {
            this.value = value;
            return this;
        };

        /**
         * @method setLink
         * @param name {string} link name
         * @param value {AssetLink} may be null
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setLink = function( name, value ) {
            this.links[ name ] = value;
            return this;
        };

        /**
         * Same as setLink( "owner", value )
         * @method setOwner
         * @param value {AssetLink} may be null
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setOwner = function( value ) {
            this.setLink( "owner", value );
            return this;
        };

        /**
         * Same as setLink( "acl", value )
         * @method setAcl
         * @param value {AssetLink} may be null
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setAcl = function( value ) {
            this.setLink( "acl", value );
            return this;
        };
        /**
         * Same as setLink( "creator", value )
         * @method setCreator
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setCreator = function( value ) {
            this.setLink( "creator", value );
            return this;
        };
        /**
         * Same as setLink( "lastUpdater", value )
         * @method setLastUpdater
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setLastUpdater = function( value ) {
            this.setLink( "lastUpdater", value );
            return this;
        }

        /**
         * Same as setLink( "to", value )
         * @method setTo
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setTo = function( value ) {
            this.setLink( "to", value );
            return this;
        }

        /**
         * Same as setLink( "from", value )
         * @method setFrom
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setFrom = function( value ) {
            this.setLink( "from", value );
            return this;
        }
        /**
         * Same as setLink( "home", value )
         * @method setHome
         * @return {AssetBuilder} this
         */
        AssetBuilder.prototype.setHome = function( value ) {
            this.setLink( "home", value );
            return this;
        }


        /**
         * Build an Asset with this builder's properties set
         * @return {Asset}
         */
        AssetBuilder.prototype.build = function() {
            return new Asset(this)
        };

        // ......................................................

        /**
         * Function builds a TestSuite for the Model module
         * @param Y {YUI} YUI instance
         * @return YUI.Test.Suite
         */
        var testSuiteBuilder = function(Y) {
            var suite = new Y.Test.Suite( "littleware.browser.Model.TestSuite" );
            var pathUtilTestCase = {
                name: "PathUtil TestCase",
                //---
                // Tests
                //---
                testPathUtil: function() {
                    var parent = PathUtil.getParent("/parent/bla");
                    Y.Assert.isTrue( "/parent" == parent, "Got expected parent: " + parent );
                    parent = PathUtil.getParent("/parent/bla/.");
                    Y.Assert.isTrue( "/parent" == parent, "Got expected parent: " + parent );
                    var name = PathUtil.getName( "/parent/bla" );
                    Y.Assert.isTrue( "bla" == name, "Got expected name: " + name );
                }
            };
            var assetTestCase = {
                name: "AssetBuilder TestCase",
                testAssetBuilder: function() {
                    var builder = new AssetBuilder();
                    var testData = {
                        owner : new AssetLink( "owner", "owner", "/owner" ),
                        acl : new AssetLink( "acl", "acl", "/acl" ) ,
                        to : new AssetLink( "to", "to", "/to" ),
                        from : new AssetLink( "from", "from", "/from" ),
                        creator : new AssetLink( "creator", "creator", "/creator" ),
                        updater : new AssetLink( "updater", "updater", "/updater" ),
                        id : "id",
                        comment : "comment",
                        path: "/path",
                        updateComment: "updateComment",
                        value: 55
                    };
                    var asset = builder.setId( testData["id"]
                        ).setComment( testData["comment"]
                        ).setUpdateComment(testData["update"]
                        ).setPath( testData["path"]
                        ).setOwner( testData["owner"]
                        ).setAcl( testData["acl"]
                        ).setTo( testData["to"]
                        ).setFrom( testData["from"]
                        ).setCreator( testData["creator"]
                        ).setLastUpdater( testData["updater"]
                        ).setUpdateComment( testData["updateComment"]
                        ).setValue( testData["value"]
                        ).build();
                    Y.log( "Asset checks running", "info", "littleware.browser.Model" );
                    Y.Assert.isTrue( asset.id == testData["id"], "id property ok")
                    Y.Assert.isTrue( asset.comment == testData["comment"], "comment property ok" )
                    Y.Assert.isTrue( asset.updateComment == testData["updateComment"], "updateComment property ok" )
                    Y.Assert.isTrue( asset.path == testData["path"], "path property ok" )
                    Y.Assert.isTrue( asset.value == testData["value"], "value property ok" )
                    Y.log( "link check", "info", "littleware.browser.Model" )
                    for( var key in asset.links ) {
                        Y.log( key + " -- " + asset.links[key], "info", "littleware.browser.Model" );
                    }
                    Y.log( "Check updater", "info", "littleware.browser.Model" );
                    Y.Assert.isTrue( asset.getLastUpdater().equals( testData["updater"] ) )
                    Y.log( "Check to", "info", "littleware.browser.Model" );
                    Y.Assert.isTrue( asset.getTo().equals( testData["to"] ) )
                    Y.log( "Check from", "info", "littleware.browser.Model" );
                    Y.Assert.isTrue( asset.getFrom().equals( testData["from"] ) )
                    Y.log( "Check acl", "info", "littleware.browser.Model" );
                    Y.Assert.isTrue( asset.getAcl().equals( testData["acl"] ) )
                    Y.log( "Check creator", "info", "littleware.browser.Model" );
                    Y.Assert.isTrue( asset.getCreator().equals( testData["creator"] ) )
                    Y.log( "Check owner", "info", "littleware.browser.Model" );
                    Y.Assert.isTrue( asset.getOwner().equals( testData["owner"] ) )
                }
            };
            suite.add( new Y.Test.Case( pathUtilTestCase ) );
            suite.add( new Y.Test.Case( assetTestCase ) );
            return suite;
        }

        // ......................................................
    
        return {
            ModelBuilder: ModelBuilder,
            AssetLink: AssetLink,
            AssetBuilder: AssetBuilder,
            PathUtil: PathUtil,
            testSuiteBuilder: testSuiteBuilder
        };
    } ();
}, "0.0" );

