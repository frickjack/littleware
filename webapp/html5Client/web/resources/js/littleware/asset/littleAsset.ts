declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-base";
}

import importY = require("../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;
Y = exports;

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
         * Lookup an asset type by id
         * @member lookup
         * @static
         * @return {AssetType} undefined if none present
         */
        static lookup(id: string): AssetType {
            return _typeIndex[id];
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
        private dateCreated: Date;
        private dateUpdated: Date;
        private startDate: Date;
        private endDate: Date;
        private fromId: string;
        private toId: string;
        private aclId: string;
        private ownerId: string;
        private otherProps: StringProps;
        private comment: string;
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
            this.dateCreated = builder.dateCreated;
            this.dateUpdated = builder.dateUpdated;
            this.startDate = builder.startDate;
            this.endDate = builder.endDate;
            this.fromId = builder.fromId;
            this.toId = builder.toId;
            this.aclId = builder.aclId;
            this.ownerId = builder.ownerId;
            this.otherProps = new StringProps( builder.otherProps );
            this.comment = builder.comment;
            this.data = builder.data;
            this.state = Math.floor(builder.state);
            this.value = builder.value;

            Y.assert( 
                this.id && this.assetType && this.name && 
                ((this.assetType.id == HomeAsset.HOME_TYPE.id) || (this.fromId && (this.id != this.fromId))), 
                "asset passes basic validation"
                );
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
        getDateCreated(): Date { return this.dateCreated; }
        getDateUpdated(): Date { return this.dateUpdated; }
        getStartDate(): Date { return this.startDate; }
        getEndDate(): Date { return this.endDate; }
        getFromId(): string { return this.fromId; }
        getToId(): string { return this.toId; }
        getAclId(): string { return this.aclId; }
        getOwnerId(): string { return this.ownerId; }
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
        private static _idFactory: IdFactory = {
            get: function () { return "" + (new Date()).getTime() + "-" + Math.floor( 100 * Math.random() ); }
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
         * Unique id - default value initialized via IdFactories.get().get()
         * @property id
         * @type string
         */
        id: string = IdFactories.get().get();
        /**
         * Chainable id setter
         * @method withId
         * @chainable
         */
        withId(value: string): AssetBuilder { this.id = value; return this; }
        /**
         * Chainable id setter - assigned new random id - useful for copying an asset as a template ...
         * @method withNewId
         * @chainable
         */
        withNewId(): AssetBuilder { this.id = IdFactories.get().get(); return this; }
        
        timestamp: number = -1;
        /**
         * @method withTimestamp
         * @chainable
         */
        withTimestamp(value: number): AssetBuilder { this.timestamp = value; return this; }

        name: string = null;
        withName(value: string): AssetBuilder { this.name = value; return this; }

        dateCreated: Date = new Date();
        withDateCreated(value: Date): AssetBuilder { this.dateCreated = value; return this; }

        dateUpdated: Date = new Date();
        withDateUpdated(value: Date): AssetBuilder { this.dateUpdated = value; return this; }

        startDate: Date = null;
        withStartDate(value: Date): AssetBuilder { this.startDate = value; return this; }

        endDate: Date = null;
        withEndDate(value: Date): AssetBuilder { this.endDate = value; return this; }

        fromId: string = null;
        withFromId(value: string): AssetBuilder { this.fromId = value; return this; }

        toId: string = null;
        withToId(value: string): AssetBuilder { this.toId = value; return this; }

        aclId: string = null;
        withAclId(value: string): AssetBuilder { this.aclId = value; return this; }

        ownerId: string = null;
        withOwnerId(value: string): AssetBuilder { this.ownerId = value; return this; }

        /**
         * Convenience method - shortcut for:
         *    withFromId( parent.getId() ).withAclId( parent.getAclId() )
         */
        withParent(parent: Asset): AssetBuilder {
            return this.withFromId(parent.getId()).withAclId(parent.getAclId());
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

        comment: string = "";
        withComment(value: string): AssetBuilder { this.comment = value; return this; }

        data: string = "";
        withData(value: string): AssetBuilder { this.data = value; return this; }

        state: number = 0;
        withState(value: number): AssetBuilder { this.state = Math.floor(value); return this; }

        value: number = 0;
        withValue(value: number): AssetBuilder { this.value = value; return this; }

        /**
         * Extract property values from the given raw "any" object -
         *   useful for narrowing generic JSON from XHR or local storage or whatever
         * @method extractRaw
         * @chainable
         */
        extractRaw(raw: any): AssetBuilder {
            this.withId( raw.id
                            ).withName( raw.name
                            ).withTimestamp( typeof( raw.timestamp ) == 'number' ? raw.timestamp : -1
                            ).withFromId(raw.fromId
                            ).withToId(raw.toId
                            ).withAclId(raw.aclId
                            ).withComment(raw.comment
                            ).withState(raw.state
                            ).withValue( raw.value
                            ).withData(raw.data
                            ).addProps( new StringProps( raw.otherProps || {} )
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
