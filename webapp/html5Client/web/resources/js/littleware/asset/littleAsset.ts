declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-base";
}

import importY = require("../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;

var lw: any = exports.littleware;

/**
 * @module littleware-asset-base
 * @namespace littleware.asset
 */
export module littleware.asset {
    var log = new lw.littleUtil.Logger("littleware.asset");
    log.log("littleware logger loaded ...");



    /**
     * Properties on a CacheObject should be treated as read-only
     *
     * @class CacheObject
     */
    export interface CacheObject {
        id: string;
        timestamp: number;
    }


    /**
     * Clean id strings - 
     * @for littleware.asset
     * @static
     * @method cleanId
     * @return {String} clean id value.replace(/\W+/g, "").toUpperCase()
     */
    export function cleanId(id: string): string {
        return (id ? id.replace(/\W+/g, "").toUpperCase() : null);
    }


    var _typeIndex: {
        [id: string]: AssetType;
    } = {};

    /**
     * Enumeration of different types of asset.
     *
     * @class AssetType
     */
    export class AssetType implements CacheObject {
        constructor(
            public id: string,
            public timestamp: number,
            public name: string
            ) {
        }

        /**
         * Factory for new builders of this asset type.
         * @member newBuilder
         */
        newBuilder(): AssetBuilder {
            throw new Error("This method is abstract");
        }

        /**
         * Lookup an asset type by id - auto-register new type if not found in index
         * @member lookup
         * @static
         * @param id {string} UUID associated with type
         * @param optName {string} name to associate with auto-register if
         *             type not already registered
         * @return {AssetType} undefined if none present
         */
        static lookup(id: string, optName?:string ): AssetType {
            var cleanId = id.replace(/\W+/g, "").toUpperCase();
            var lookup = _typeIndex[cleanId];
            if (lookup) {
                return lookup;
            }
            var name = optName ? optName : "unknown-" + cleanId;
            lookup = new AssetType(cleanId, -1, name );
            AssetType.register(lookup);
            return lookup;
        }

        /**
         * Register a new asset type with the lookup index
         * @member register
         * @param value {AssetType}
         * @static
         */
        static register(value: AssetType): void {
            if (_typeIndex[value.id]) {
                throw new Error("Asset type with id already registered: " + value.id);
            }
            _typeIndex[value.id] = value;
        }
    }

    /**
     * Read-only view of string properites
     */
    export class StringProps {

        constructor(private props: { [key: string]: string; }) {
        }

        keys(): string[]{
            var keys: string[] = [];
            for (var key in this.props) {
                keys.push(key);
            }
            return keys;
        }
        
        get (key: string): string { return this.props[key]; }
    }


    /**
     * Abstract base class for different asset types.
     * Node in littleware repository database tree.
     * Properties should be treated read-only - use
     * an AssetBuilder to assemble an udpated copy, and
     * save it back to the repo via something like:
     *     var updatedAsset = assetManager.save( asset.getBuilder()...build() );
     *
     * @class Asset
     */
    export class Asset {
        private id: string;
        private assetType: AssetType;
        private timestamp: number;
        private name: string;
        private creatorId: string;
        private dateCreated: Date;
        private updaterId: string;
        private dateUpdated: Date;
        private startDate: Date;
        private endDate: Date;
        private homeId: string;
        private fromId: string;
        private toId: string;
        private aclId: string;
        private ownerId: string;
        private otherProps: StringProps;
        private comment: string;
        private updateComment: string;
        private data: string;
        private state: number;
        private value: number;

        /**
         * Intended to be protected - implement subtypes and builders with
         *    hooks via AssetType.newBuilder - ex: GenericAsset.Type.newBuilder()...
         * @constructor
         * @param builder {AssetBuilder}
         */
        constructor ( builder:AssetBuilder 
        ) {
            this.id = builder.id;
            this.assetType = builder.getAssetType();
            this.timestamp = builder.timestamp;
            this.name = builder.name;
            this.creatorId = builder.creatorId;
            this.dateCreated = builder.dateCreated;
            this.updaterId = builder.updaterId;
            this.dateUpdated = builder.dateUpdated;
            this.startDate = builder.startDate;
            this.endDate = builder.endDate;
            this.homeId = builder.homeId;
            this.fromId = builder.fromId;
            this.toId = builder.toId;
            this.aclId = builder.aclId;
            this.ownerId = builder.ownerId;
            this.otherProps = new StringProps( builder.otherProps );
            this.comment = builder.comment;
            this.updateComment = builder.updateComment;
            this.data = builder.data;
            this.state = Math.floor(builder.state);
            this.value = builder.value;

            var isValid = this.assetType && this.name &&
                ((this.assetType.id == HomeAsset.HOME_TYPE.id) || (this.homeId && this.fromId && (this.id != this.fromId)));
            if (!isValid) {
                log.info("Asset constructor fails validity check");
                console.dir(this);
            }
            Y.assert( isValid,"asset passes basic validation");
        }

        /**
         * Access read-only id that uniquely identifies this asset
         * @method getId
         * @return {string}
         */
        getId(): string { return this.id; }
        /**
         * @method getAssetType
         * @return {AssetType}
         */
        getAssetType(): AssetType { return this.assetType; }

        /**
         * Get the timestamp at which this version of the asset
         * was last saved to the database - basically a last-update count or Etag
         * - can be used to determine if a local copy of an asset is in sync with the repository
         * @method getTimestamp()
         * @return {number} 
         */
        getTimestamp(): number { return this.timestamp; }

        /**
         * @method getName
         * @return {string}
         */
        getName(): string { return this.name; }
        getCreatorId(): string {
            return this.creatorId;
        }
        getDateCreated(): Date { return this.dateCreated; }
        getUpdaterId(): string {
            return this.updaterId;
        }
        getDateUpdated(): Date { return this.dateUpdated; }
        getStartDate(): Date { return this.startDate; }
        getEndDate(): Date { return this.endDate; }
        getHomeId(): string {
            return this.homeId;
        }

        getFromId(): string { return this.fromId; }
        getToId(): string { return this.toId; }
        getAclId(): string { return this.aclId; }
        getOwnerId(): string { return this.ownerId; }
        getUpdateComment(): string {
            return this.updateComment;
        }
        getComment(): string { return this.comment; }

        /**
         * @method getState
         * @return {int}
         */
        getState(): number { return this.state; }
        /**
         * Data block attached to this asset in the db - at most 1000 characters.
         * Subtypes often extract extra properties from the databucket.
         * @method getData
         * @return {string}
         */
        getData(): string { return this.data; }
        /**
         * Value field - different subtypes use differently (weight, cost, whatever)
         * @member getValue
         * @return {number}
         */
        getValue(): number { return this.value; }

        getOtherProps(): StringProps { return this.otherProps; }

        /**
         * Shortcut for getAssetType().newBuilder()... and a copy of all this
         * objects properties, so actually returns a subtype of AssetBuilder 
         * appropriate for this object's asset type.
         * @method copy
         * @return {AssetBuilder}
         */
        copy(): AssetBuilder {
            return this.assetType.newBuilder().extractRaw(this);
        }
    }


    /**
     * Factory for unique-ish id strings
     * @class IdFactory
     */
    export interface IdFactory {
        /**
         * @method get
         */
        get (): string;
    }


    /**
     * Static methods associated with IdFactory interface
     * @class IdFactories
     */
    export class IdFactories {
        private static counter: number = 0;

        private static _idFactory: IdFactory = {
            get: function () {
                IdFactories.counter += 1;
                return cleanId( "" + (new Date()).getTime() + Math.floor(100 * Math.random()) + IdFactories.counter );
            }
        };

        /**
         * Allocate an id-factory - just a singleton for this implementation
         * @method get
         */
        static get (): IdFactory { return IdFactories._idFactory; }
    }


    
    /**
     * Abstract base class - a builder for a particular asset type
     * can extend and customize this.
     * @class AssetBuilder
     */
    export class AssetBuilder {
        constructor(private assetType: AssetType) { }

        /**
         * A builder builds an asset of a particular asset type
         * @method getAssetType
         */
        getAssetType(): AssetType { return this.assetType; }

        /**
         * Id of the "home" asset that roots the tree this asset is under.
         * Must be non-null for all non-home assets.
         * @property homeId
         */
        homeId: string = null;
        /** 
         * Chainable homeId setter with cleanId filter
         * @method withHomeId
         * @chainable
         */
        withHomeId(value: string): AssetBuilder {
            this.homeId = cleanId(value); return this;
        }

        /**
         * Unique id - default value null - then assigned by AssetMgr.saveAsset
         * @property id
         * @type string
         */
        id: string = null;  // IdFactories.get().get();
        /**
         * Chainable id setter with cleanId filter
         * @method withId
         * @chainable
         */
        withId(value: string): AssetBuilder { this.id = cleanId(value); return this; }
        /**
         * Chainable id setter - assigned new random id - useful for copying an asset as a template ...
         * @method withNewId
         * @chainable
         */
        withNewId(): AssetBuilder { this.id = cleanId(IdFactories.get().get()); return this; }
        
        /**
         * @property timestamp
         * @type number
         */
        timestamp: number = -1;
        /**
         * @method withTimestamp
         * @chainable
         */
        withTimestamp(value: number): AssetBuilder { this.timestamp = value; return this; }

        /**
         * @property name
         * @type string
         */
        name: string = null;

        /**
         * @method withName
         * @chainable
         */
        withName(value: string): AssetBuilder { this.name = value; return this; }

        /**
         * @property creatorId
         * @type string
         */
        creatorId: string = null;
        /**
         * @method withCreatorId
         * @chainable
         */
        withCreatorId(value: string): AssetBuilder {
            this.creatorId = cleanId(value); return this;
        }

        /**
         * @property dateCreated
         * @type Date
         */
        dateCreated: Date = new Date();
        /**
         * @method withDateCreated
         * @chainable
         */
        withDateCreated(value: Date): AssetBuilder { this.dateCreated = value; return this; }

        /**
         * @property updaterId
         * @type string
         */
        updaterId: string = null;
        /**
         * @method withUpdaterId
         * @chainable
         */
        withUpdaterId(value: string): AssetBuilder {
            this.updaterId = cleanId(value); return this;
        }

        /**
         * @property dateUpdated
         * @type Date
         */
        dateUpdated: Date = new Date();
        /**
         * @method withDateUpdated
         * @chainable
         */
        withDateUpdated(value: Date): AssetBuilder { this.dateUpdated = value; return this; }

        /**
         * @property startDate
         * @type Date
         */
        startDate: Date = null;
        /**
         * @method withStartDate
         * @chainable
         */
        withStartDate(value: Date): AssetBuilder { this.startDate = value; return this; }

        /**
         * @property endDate
         * @type Date
         */
        endDate: Date = null;
        /**
         * @method withEndDate
         * @chainable
         */
        withEndDate(value: Date): AssetBuilder { this.endDate = value; return this; }

        /**
         * @property fromId
         * @type string
         */
        fromId: string = null;
        /**
         * @method withFromId
         * @chainable
         */
        withFromId(value: string): AssetBuilder { this.fromId = cleanId(value); return this; }

        /**
         * @property toId
         * @type string
         */
        toId: string = null;
        /**
         * @method withToId
         * @chainable
         */
        withToId(value: string): AssetBuilder { this.toId = cleanId(value); return this; }

        /**
         * @property aclId
         * @type string
         */
        aclId: string = null;
        /**
         * @method withAclId
         * @chainable
         */
        withAclId(value: string): AssetBuilder { this.aclId = cleanId(value); return this; }

        /**
         * @property ownerId
         * @type string
         */
        ownerId: string = null;
        /**
         * @method withOwnerId
         * @chainable
         */
        withOwnerId(value: string): AssetBuilder { this.ownerId = cleanId(value); return this; }

        /**
         * Convenience method - shortcut for:
         *    withFromId( parent.getId() ).withAclId( parent.getAclId() )
         * @method withParent
         * @chainable
         */
        withParent(parent: Asset): AssetBuilder {
            return this.withFromId(parent.getId()).withHomeId( parent.getHomeId() ).withAclId(parent.getAclId());
        }

        otherProps: { [key: string]: string; } = {};
        addProp(key: string, value: string): AssetBuilder { this.otherProps[key] = value; return this; }
        addProps(props: StringProps): AssetBuilder {
            var keys = props.keys(), i = 0;
            for ( i = 0; i < keys.length; ++i) {
                this.otherProps[keys[i]] = props.get(keys[i]);
            }
            return this;
        }

        /**
         * @property updateComment
         * @type string
         */
        updateComment: string = "";
        /**
         * @method withUpdateComment
         * @chainable
         */
        withUpdateComment(value: string): AssetBuilder {
            this.updateComment = value; return this;
        }

        /**
         * @property comment
         * @type string
         */
        comment: string = "";
        /**
         * @method withComment
         * @chainable
         */
        withComment(value: string): AssetBuilder { this.comment = value; return this; }

        /**
         * @property data
         * @type string
         */
        data: string = "";
        /**
         * @method withData
         * @chainable
         */
        withData(value: string): AssetBuilder { this.data = value; return this; }

        /**
         * @property state
         * @type number
         */
        state: number = 0;
        /**
         * @method withState
         * @chainable
         */
        withState(value: number): AssetBuilder { this.state = Math.floor(value); return this; }

        /**
         * @property value
         * @type number
         */
        value: number = 0;
        /**
         * @method withValue
         * @chainable
         */
        withValue(value: number): AssetBuilder { this.value = value; return this; }

        /**
         * Extract property values from the given raw "any" object -
         *   useful for narrowing generic JSON from XHR or local storage or whatever
         * @method extractRaw
         * @chainable
         */
        extractRaw(raw: any): AssetBuilder {
            var props = raw.otherProps;
            if (props.props) { // asset's StringProps serialize via Json.stringify with private 'props' property
                props = props.props;
            }
            this.withId( raw.id
                            ).withName( raw.name
                            ).withTimestamp(typeof (raw.timestamp) == 'number' ? raw.timestamp : -1
                            ).withHomeId(raw.homeId 
                            ).withFromId(raw.fromId
                            ).withToId(raw.toId
                            ).withAclId(raw.aclId
                            ).withComment(raw.comment
                            ).withState(raw.state
                            ).withValue( raw.value
                            ).withData(raw.data
                            ).addProps(new StringProps(props || {})
                            ).withCreatorId(raw.creatorId
                            ).withUpdaterId(raw.updaterId
                            ).withUpdateComment( raw.updateComment
                            ).withDateCreated(raw.dateCreated
                            ).withDateUpdated(raw.dateUpdated
                            ).withStartDate(raw.startDate
                            ).withEndDate(raw.endDate);
            return this;
        }

        /**
         * @method build
         * @return {Asset}
         */
        build(): Asset { throw new Error("this method is abstract"); }
    }


    /**
     * Generic asset for folders or general use
     * @class GenericAsset
     */
    export class GenericAsset extends Asset {
        /**
         * @property GENERIC_TYPE 
         * @type AssetType
         * @static
         */
        static GENERIC_TYPE = new AssetType("E18D1B19D9714F6F8F49CF9B431EBF23", -1, "littleware.GENERIC");
        

    }



    GenericAsset.GENERIC_TYPE.newBuilder = function () {
        var builder = new AssetBuilder(GenericAsset.GENERIC_TYPE);
        builder.build = function () {
            log.info("Building Generic asset: ");
            console.dir(builder);
            return new GenericAsset(builder);
        };

        return builder;
    }

    AssetType.register(GenericAsset.GENERIC_TYPE);

    /**
     * Name-unique "home" asset for roots in repo node forest
     * @class HomeAsset
     */
    export class HomeAsset extends Asset {
        /**
         * @property HOME_TYPE
         * @type AssetType
         * @static
         */
        static HOME_TYPE = new AssetType("C06CC38C6BD24D48AB5E2D228612C179", -1, "littleware.HOME" );
    }

    HomeAsset.HOME_TYPE.newBuilder = function () {
        var builder = new AssetBuilder(HomeAsset.HOME_TYPE);
        builder.build = function () {
            builder.fromId = null;  // home assets don't have parents
            builder.homeId = builder.id;
            return new HomeAsset(builder);
        };

        return builder;
    }

    AssetType.register(HomeAsset.HOME_TYPE);


    /**
     * Asset acts as a reference (link) to another asset in the repository node tree
     * @class LinkAsset
     */
    export class LinkAsset extends Asset {
        /**
         * @property LINK_TYPE
         * @type AssetType
         * @static
         */
        static LINK_TYPE = new AssetType("926D122F82FE4F28A8F5C790E6733665", -1, "littleware.LINK" );
    }

    LinkAsset.LINK_TYPE.newBuilder = function () {
        var builder = new AssetBuilder(LinkAsset.LINK_TYPE);
        builder.build = function () {
            return new HomeAsset(builder);
        };
        return builder;
    }

    AssetType.register(LinkAsset.LINK_TYPE);


}
