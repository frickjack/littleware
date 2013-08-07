/// <reference path="../../libts/yui.d.ts" />
declare var exports:any;

var Y:Y = exports;

if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-base";
}


var lw = exports.littleware;

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
    export class Asset implements CacheObject {
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

            Y.assert(this.id && this.assetType && this.name && 
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
            get: function () { return "" + (new Date()).getTime() + "/" + Math.random(); }
        };

        /**
         * Allocate an id-factory - just a singleton for this implementation
         * @method get
         */
        static get (): IdFactory { return _idFactory; }
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

    //----------------------------------

    /**
     * Event type passed to an AssetRef listener to let it know when
     * the referenced asset has changed, or some change to its children
     * has occurred.
     *
     * @class RefEvent
     */
    export class RefEvent {
        constructor(
            /**
             * One of the RefEvent.EVENT_TYPES
             * @property eventType
             * @type string
             */
            public eventType: string,
            /**
             * Id of the changed asset (usually child id)
             * @property id
             * @type string
             */
            public id: string) { }

        static EVENT_TYPES = {
            ASSET_CHANGED: "ASSET_CHANGED",
            CHILD_CHANGED: "CHILD_CHANGED",
            CHILD_ADDED: "CHILD_ADDED",
            CHILD_REMOVED: "CHILD_REMOVED"
        }
    }


    /**
     * Reference maintained by client-side asset cache internal to the AssetManager.
     * The referenced asset changes changes when the AssetManager
     * receives an updated asset either via a local save or an update reflecting
     * some other client's change to the repository.
     * A reference may also be empty - an empty reference might be returned
     * by AssetManager.loadAsset or whatever.
     * Finally - AssetRef implements a reference-counting scheme that helps the backend
     * AssetManager decide when it's ok to remove a referenced asset from cache.
     * Client code should invoke the detach() method to decrement a reference's count
     * when the reference is no longer needed.
     *
     * @class AssetRef
     */
    export interface AssetRef {
        /**
         * @method getAsset
         * @return {Asset} the asset currently referenced or null if isEmpty()
         */
        getAsset(): Asset;

        /**
         * @method addListener
         * @param listener {function}
         * @return YUI subscription with "detach" method to unsubscribe
         */
        addListener(listener: (ev: RefEvent) => void ): Y.EventHandle;

        /** 
         * True if null reference 
         * @method isEmpty
         */
        isEmpty(): bool;

        /**
         * ! isEmpty
         * @method isDefined
         */
        isDefined(): bool;

        /**
         * Invoke the given function on the referenced asset if isDefined
         * @method map
         * @param lambda
         * @return lambda( getAsset() ) if isDefined or null if isEmpty
         */
        map(lambda: (Asset) => any): any;

        /**
         * Decrement the reference account associated with this ref
         * @method detach
         */
        detach(): void;
    }

    /**
     * name-id tuple for AssetManager.listChildren and similar methods
     * @class NameIdPair
     */
    export class NameIdPair {
        constructor( public name: string, public id: string) { }
    }

    /**
     * API tool for interacting with the asset repo.
     * @class AssetManager
     */
    export interface AssetManager {
        /**
         * @method saveAsset
         * @return {Y.Promise[AssetRef]}
         */
        saveAsset(value: Asset, updateComment: string): Y.Promise;
        /**
         * @method deleteAsset
         * @return {Y.Promise[string]}
         */
        deleteAsset(id: string, deleteComment: string): Y.Promise;

        /**
         * Return a reference to the asset with the given id if any - otherwise null.
         * @method loadAsset
         * @return {Y.Promise[AssetRef]}
         */
        loadAsset(id: string): Y.Promise;

        /**
         * Load the child of the given parent with the given name -
         * every child has a unique name within its set of siblings.
         * Usually implemented as a shortcut for loadAsset( listChildren( parentId )[name] ).
         * @method loadChild
         * @param parentId {string}
         * @param name {string}
         * @return {Y.Promise{AssetRef}}
         */
        loadChild(parentId: string, name: string): Y.Promise;

        /**
         * Load the asset at the given path if any
         * @method loadPath
         * @param path {string}
         * @return {Y.Promise{AssetRef}}
         */
        loadPath(path: string): Y.Promise;

        /**
         * List the children if any under the given parent node
         * @method listChildren
         * @param parentId {string}
         * @return {Y.Promise[NameIdPair]}
         */
        listChildren(parentId: string): Y.Promise;

        /**
         * List the root (littleware.HOME-TYPE) nodes - shortcut for listChildren(null)
         * @method listRoots
         * @return {Y.Promise[NameIdPair]}
         */
        listRoots(): Y.Promise;
    }

    /**
     * Factory method to acquire the AssetManager singleton - add sesion management later
     */
    export function getAssetManager():AssetManager {
        return localMgr;
    }


    /**
     * Internal manager implementation extends AssetManager
     * with a getFromCache() method for accessing AssetRefs that
     * have already been loaded into memory by the app for triggering
     * update events on listeners and that kind of thing.
     *
     * @class InternalManager
     */
    interface InternalManager extends AssetManager {
        /**
         * For accessing AssetRefs that
         * have already been loaded into memory by the app for triggering
         * update events on listeners and that kind of thing.
         *
         * @method getFromCache
         * @param id {string}
         * @return {AssetRef}
         */
        getFromCache(id: string): AssetRef;
    }


    /**
     * Internal implementation of AssetRef
     * @class InternalAssetRef
     */
    class InternalAssetRef implements AssetRef {
        // exports == Y, but has any type instead of bogus yui.d.ts Interface
        private target: Y.EventTarget = new exports.EventTarget();
        refCount = 1;

        constructor(private asset: Asset, private mgr: InternalManager) {
            if (null != asset) {
                Y.assert(mgr && true, "Must provide manager reference for non-empty reference");
            }
        }

        detach(): void {
            if (this.refCount > 0) {
                this.refCount--;
            }
        }

        /**
         * Fire an assetRefEvent for any listeners
         * @method fire
         */
        fire(ev: RefEvent): void {
            this.target.fire("assetRefEvent", <any> ev);
        }


        /**
         * Internal utility - resets target with the new value, and fires
         * event to listeners if 
         * value.timeStamp >= asset.timeStamp,
         * also looks for parent asset if any in local repo.
         *
         * @method update
         * @param value {Asset}
         */
        updateAsset(value: Asset): void {
            Y.assert(value.getId() == this.asset.getId(), "Update id =? reference id");
            if (value.getTimestamp() >= this.asset.getTimestamp()) {
                var old = this.asset;
                this.asset = value;
                //log.log("Firing update event for " + this.asset.getId());
                this.fire( new RefEvent(RefEvent.EVENT_TYPES.ASSET_CHANGED, value.getId()));
                if (old.getFromId() != value.getFromId()) {
                    if (null != old.getFromId()) {
                        var oldParent = <InternalAssetRef> this.mgr.getFromCache(old.getFromId());
                        if (oldParent.isDefined()) {
                            oldParent.fire(new RefEvent(RefEvent.EVENT_TYPES.CHILD_REMOVED, value.getId()));
                        }
                    }
                    if (null != value.getFromId()) {
                        var newParent = <InternalAssetRef> this.mgr.getFromCache(value.getFromId());
                        if (newParent.isDefined()) {
                            newParent.fire( new RefEvent(RefEvent.EVENT_TYPES.CHILD_ADDED, value.getId()));
                        }
                    }
                } else if (null != value.getFromId()) {
                    var parent = <InternalAssetRef> this.mgr.getFromCache(value.getFromId());
                    if (parent.isDefined()) {
                        parent.fire( new RefEvent(RefEvent.EVENT_TYPES.CHILD_CHANGED, value.getId()));
                    }
                }
            }
        }

        getAsset(): Asset { return this.asset; }

        addListener(listener: (ev: RefEvent) => void ): Y.EventHandle {
            return this.target.on("assetRefEvent", listener);
        }

        isEmpty(): bool { return this.asset == null; }

        isDefined(): bool { return !this.isEmpty(); }

        map(lambda: (Asset) => any): any {
            if (this.isEmpty()) {
                return null;
            }
            return lambda(this.asset);
        }

    }

    var EmptyRef = new InternalAssetRef(null, null);


    //Y.augment( <any> AssetRef, <any> Y.EventTarget);

    //-----------------------------------

    /**
     * InternalManager implementation that stores its repository in the 
     * HTML5 local cache.  Note - 5MB storage limit in most browsers.
     * @class LocalCacheManager
     */
    class LocalCacheManager implements InternalManager {
        //
        // TODO - eventually will need to eject old data from in-memory cache (check littleUtil.Cache),
        //  but just keep everything in memory for now - just building little toy apps
        //
        private cache: { [key: string]: InternalAssetRef; } = {};
        private childCache: { [key: string]: NameIdPair[]; } = {};

        timestamp = 0;
        static nodePrefix = "assetMan/nodes/";
        static childPrefix = "assetMan/children/";
        static confPrefix = "assetMan/conf/";

        private storage = {
            getItem: function (key: string): string { return null; },
            setItem: function ( key:string, value:string ):void { },
            removeItem: function ( key:string ):void { }
        };

        constructor() {
            if ( typeof (localStorage) != 'undefined' ) {
                this.storage = localStorage;
                var tsEntry = this.storage.getItem( LocalCacheManager.confPrefix + "timestamp");
                if (tsEntry) {
                    this.timestamp = parseInt(tsEntry);
                }
            }
        }

        /** 
         * Internal synchronous list children implementation - just accesses
         * cache or local storage directly.
         */
        private _listChildren(parentId: string): NameIdPair[]{
            parentId = parentId || "homeRoot";
            var result: NameIdPair[] = this.childCache[parentId];
            if (!result) {
                result = JSON.parse(this.storage.getItem(LocalCacheManager.childPrefix + parentId)) || [];
                this.childCache[parentId] = result;
            }
            return result;
        }

        /**
         * Internal save-child data 
         */
        private _saveChildren(parentId: string, data: NameIdPair[]): void {
            parentId = parentId || "homeRoot";
            this.childCache[parentId] = data;
            this.storage.setItem(LocalCacheManager.childPrefix + parentId,
                JSON.stringify(data)
                );
        }

        

        saveAsset(value: Asset, updateComment: string): Y.Promise {
            return this.loadAsset(value.getId()).then((refIn) => {
               var ref = <InternalAssetRef> refIn;
               this.timestamp++;
               if (value.getTimestamp() > this.timestamp) {
                    this.timestamp = value.getTimestamp() + 1;
               }

               var copy = value.copy().withTimestamp(this.timestamp).withDateUpdated(new Date()).build();

               var oldParentId = null;
               var oldName = null;
               var newParentId = copy.getFromId();

               if (!ref.isEmpty()) {
                   oldParentId = ref.getAsset().getFromId();
                   oldName = ref.getAsset().getName();
               }
               if (copy.getAssetType().id === HomeAsset.HOME_TYPE.id) {
                   newParentId = "homeRoot";
               } else if (newParentId == null) {
                   throw new Error("Must specify asset's fromId unless it's a HOME-type asset");
               } // TODO - verify an asset has a clean path to a root (is not a child of a descendent - ugh)

                // update the child-lists on the old and new parent
               if ( (newParentId !== oldParentId) || (copy.getName() !== oldName) ) { // update children info
                   // first - verify that the new name or parent doesn't have a name collision with its children
                   var newParentChildren: NameIdPair[] = this._listChildren( newParentId );
                   newParentChildren = Y.Array.reject(newParentChildren, function (item) { return item.id == copy.getId(); });
                   if (Y.Array.find(newParentChildren, function (it) { return it.name == copy.getName(); })) {
                       throw new Error("Asset already exists under new parent with name: " + copy.getName());
                   }
                   newParentChildren.push(new NameIdPair(copy.getName(), copy.getId()));
                   this._saveChildren( newParentId, newParentChildren );

                   // update old parent if any
                   if (oldParentId && (oldParentId != copy.getFromId()) ) {  
                       var oldParentChildren: NameIdPair[] = this._listChildren( oldParentId );
                       if (oldParentChildren.length > 0) {
                           oldParentChildren = Y.Array.reject(oldParentChildren, function (item) { return item.id == copy.getId(); });
                           this._saveChildren( oldParentId, oldParentChildren );
                       }
                   }

               }

                // if we made it this far, then the asset passed whatever validation we have
               this.storage.setItem(LocalCacheManager.nodePrefix + copy.getId(), JSON.stringify(copy));
               this.storage.setItem(LocalCacheManager.confPrefix + "timestamp", "" + this.timestamp);

                // finally - update the in-memory cache and reference
               if (!ref.isEmpty()) {
                   //log.log("Updating cached asset reference: " + copy.getId() + " - " + copy.getTimestamp() );
                   ref.updateAsset(copy);
                } else {
                   ref = new InternalAssetRef(copy, this);
                   this.cache[copy.getId()] = ref;
                }
                return ref;
            });
        }

    
        listChildren(parentId: string): Y.Promise {
            return new Y.Promise((resolve, reject) => {
                var children: NameIdPair[] = this._listChildren( parentId );
                resolve(children);
            });
        }

        listRoots(): Y.Promise {
            return this.listChildren( null );
        }

        loadAsset(id: string): Y.Promise {
            return new Y.Promise((resolve, reject) => {
                var ref = EmptyRef;
                if (this.cache[id]) {
                    ref = this.cache[id];
                    ref.refCount++;
                } else {
                    var json = this.storage.getItem(LocalCacheManager.nodePrefix + id);
                    if (json) {
                        var info = JSON.parse(json);
                        var builder = AssetType.lookup(info.assetType.id).newBuilder()
                        var asset = builder.extractRaw(info).build();
                        ref = new InternalAssetRef(asset, this);
                        this.cache[id] = ref;
                    }
                } 
                resolve(ref);  
            });

        }

        loadChild(parentId: string, name: string): Y.Promise {
            return this.listChildren(parentId).then(
                (siblings) => {
                    var childInfo:NameIdPair = Y.Array.find(siblings, (it) => {
                        return it.name === name;
                    });
                    if (childInfo) {
                        return this.loadAsset(childInfo.id);
                    } else {
                        return new Y.Promise((resolve) => resolve(EmptyRef));
                    }
                }
            );
        }

        /**
         * Internal helper - traverse path recursively
         */
        private _loadPath(parentId: string, partsLeft: string[]): Y.Promise {
            var name = partsLeft.shift();
            //log.log("_loadPath loading " + parentId + " child " + name);
            var childPromise = this.loadChild(parentId, name);
            if (partsLeft.length > 0) {
                return childPromise.then(
                    (ref: AssetRef) => {
                        if (ref.isEmpty() ) {
                            //log.log("_loadPath EMPTY result for " + parentId + " child " + name + ", partsLeft: " + partsLeft.length );
                            return new Y.Promise((resolve) => {
                                resolve(ref);
                            });
                        } else {
                            //log.log("_loadPath got result for " + parentId + " child " + name + ", partsLeft: " + partsLeft.length);
                            return this._loadPath(ref.getAsset().getId(), partsLeft);
                        }
                    }
                );
            } else {
                return childPromise;
            }
        }

        loadPath(path: string): Y.Promise {
            var parts = Y.Array.filter(path.split(/\/+/), (it) => { return it && true; });
            if (parts.length < 1) {
                return new Y.Promise((resolve, reject) => {
                    reject(new Error("failed to parse path: " + path));
                });
            }
            return this._loadPath(null, parts);
        }

        getFromCache(id: string): AssetRef {
            var ref = this.cache[id];
            return ref || EmptyRef;
        }

        deleteAsset(id: string, deleteComment: string): Y.Promise {
            return Y.Promise.batch(
                this.loadAsset(id), this.listChildren(id)
             ).then(
                (dataVec) => {
                    var ref: AssetRef = dataVec[0];
                    var childList: NameIdPair[] = dataVec[1];
                    var asset = ref.getAsset();

                    // note - let the server take care of this kind of check if/when
                    //   we have a real backend ...
                    if (childList.length > 0) {
                        throw new Error("May not delete an asset with children in the tree");
                    }

                    // remove node from cache and storage
                    delete (this.cache[id]);
                    delete (this.childCache[id]);
                    this.storage.removeItem(LocalCacheManager.nodePrefix + id);

                    // update parent's list of children
                    var parentId = asset.getFromId();
                    if (asset.getAssetType().id === HomeAsset.HOME_TYPE.id) {
                        parentId = "homeRoot";
                    }
                    var siblings = this._listChildren(parentId);
                    siblings = Y.Array.reject(siblings, (it) => {
                        it.id === asset.getId();
                    });
                    this._saveChildren(parentId, siblings);
                }
            )
        }

    }

    var localMgr = new LocalCacheManager();

    //-----------------------------------

    module TestSuite {
        export var testHomeId = "D589EABED8EA43C1890DBF3CF1F9689A";
        export var homeRef: AssetRef;
        export var folderId = "63E182C6E64C4C598A0B5FAB6597B54A";
        export var testFolderRef: AssetRef;
        export var mgr = getAssetManager();

        function createIfNecessary(asset: Asset): Y.Promise {
            // make sure the "test home" has been initialized
            return mgr.loadAsset(asset.getId()).then(
                (ref: AssetRef) => {
                    if (ref.isEmpty()) { // need to create the asset - use the builder
                        log.log( "Creating asset: " + asset.getName() );
                        return mgr.saveAsset(asset, "setup test sandbox");
                    }
                    log.log("Found asset: " + asset.getName());
                    return new Y.Promise((resolve, reject) => { resolve(ref); });
                }
            );
        }

        export var setupPromise = createIfNecessary(
            HomeAsset.HOME_TYPE.newBuilder().withId(testHomeId).withName("littleware.test_home"
                ).withComment("root of test sandbox node tree").build()
        ).then((ref) => {
            homeRef = ref;
            return createIfNecessary(
                GenericAsset.GENERIC_TYPE.newBuilder().withName("testFolder"
                        ).withComment("parent asset for simple build test"
                        ).withFromId(homeRef.getAsset().getId()
                        ).withId(folderId).build()
                );
        }
        ).then(
                (ref) => {
                    log.log("Test setup looks ok ...");
                    testFolderRef = ref;
                },
                (error) => {
                    log.log("Ugh! setup failed: " + error);
                }
        );

        /**
         * Little helper - resumes test
         * @method checkSetup
         * @param testCase {TestCase}
         */
        export function checkSetup():void {
            Y.Assert.isTrue( homeRef && testFolderRef && true, "littleware.test_home setup properly - homeRef: " +
                homeRef + ", folderRef: " + testFolderRef
                );
        }
    }

    export function buildTestSuite(): Y.Test_TestSuite {
        var suite: Y.Test_TestSuite = new Y.Test.TestSuite("littleware-asset");
        var mgr = getAssetManager();

        suite.add( new Y.Test.TestCase(
                {
                    name: "Test Asset Stuff",


                    testIdFactory:function() {
                        var factory = IdFactories.get();
                        var id1 = factory.get();
                        var id2 = factory.get();
                        Y.Assert.isTrue(id1 != id2, "Id factory generating different ids: " + id1 + " ?= " + id2);
                    },

                    testSetup: function () {
                        TestSuite.setupPromise.then(() => {
                            this.resume();
                        }, () => {
                            Y.Assert.fail("Setup failed");
                        }
                        );
                        this.wait(5000);
                    },

                    testAssetBuild:function() {
                        TestSuite.checkSetup();
                        var parent = TestSuite.testFolderRef.getAsset();
                        var a1 = GenericAsset.GENERIC_TYPE.newBuilder().withName("a1"
                            ).withComment("child of parent").withFromId(parent.getId()).build();
                        var a1Copy = a1.copy().build();
                        Y.Assert.isTrue( a1.getName() == "a1", "asset build set expected name: " + a1.getName());
                        Y.Assert.isTrue(a1.getName() == a1Copy.getName(), "Copy preserves name");
                        Y.Assert.isTrue(a1.getComment() == "child of parent" && a1.getComment() == a1Copy.getComment(),
                                          "asset built with expected comment, and preserved on copy"
                                          );
                        Y.Assert.isTrue(a1.getFromId() == parent.getId() && a1Copy.getFromId() == parent.getId(),
                                     "asset and copy have expected parent"
                                    );
                        return a1;
                    },

                    testSaveLoad: function () {
                        TestSuite.checkSetup();
                        var a1: Asset = this.testAssetBuild();
                        // make name unique
                        var a2 = a1.copy().withName("a1" + (new Date()).getTime()).build();
                        var a2Path = "/littleware.test_home/testFolder/" + a2.getName();

                        var promise = mgr.saveAsset(a2, "Saving test asset").then(
                            (ref) => {
                                Y.Assert.isTrue(a2.getId() == ref.getAsset().getId(), "Id preserved on save");
                                Y.Assert.isTrue(ref.getAsset().getTimestamp() >= 0, "Timestamp updated on save");
                                return Y.batch(
                                    mgr.loadAsset(a2.getId()),
                                    mgr.listChildren(a2.getFromId()),
                                    mgr.listRoots(),
                                    mgr.loadChild( null, "littleware.test_home" ),
                                    mgr.loadChild(a2.getFromId(), a2.getName()),
                                    mgr.loadPath( a2Path )
                                    );
                            }
                        ).then(
                            (batchVec) => {
                                var ref: AssetRef, childList: NameIdPair[], rootList:NameIdPair[], homeRef:AssetRef, nameRef:AssetRef, pathRef:AssetRef;
                                if (batchVec.length > 1) {
                                    ref = batchVec[0];
                                    childList = batchVec[1];
                                    rootList = batchVec[2];
                                    homeRef = batchVec[3];
                                    nameRef = batchVec[4];
                                    pathRef = batchVec[5];

                                    log.log("Attempting to save asset with same name under test parent");
                                    // finally - try to save an asset with the same name - should fail
                                    mgr.saveAsset(ref.getAsset().copy().withId(ref.getAsset().getId() + "-2").build(),
                                                   "Try to save a copy"
                                    ).then(
                                        () => {
                                            this.resume(() => {
                                                Y.Assert.fail("Succeeded saving 2nd asset with same name under a common parent");
                                            });
                                            mgr.deleteAsset(ref.getAsset().getId(), "cleanup test node");
                                        },
                                        () => {
                                            this.resume(
                                                () => {
                                                    Y.Assert.isTrue(ref.isDefined(), "Able to load just saved asset");
                                                    var asset = ref.getAsset();
                                                    Y.Assert.isTrue(rootList.length > 0, "Non-empty root list");
                                                    Y.Assert.isTrue(homeRef.isDefined() && (homeRef.getAsset().getName() == "littleware.test_home"), "Loaded test home" );
                                                    Y.Assert.isTrue(nameRef.isDefined() && (nameRef.getAsset().getId() == asset.getId()),
                                                        "loadChild works ..."
                                                        );
                                                    Y.Assert.isTrue(pathRef.isDefined() && (pathRef.getAsset().getId() == asset.getId()),
                                                        "loadPath works ...: " + a2Path
                                                        );
                                                    Y.Assert.isTrue(asset.getId() == a2.getId(), "test load looks ok");
                                                    Y.Assert.isTrue(childList.length > 0, "test child list non empty");
                                                    Y.Assert.isTrue(Y.Array.find(childList,
                                                        function (it) { return (it.id == asset.getId()) && (it.name == asset.getName()); }
                                                        ) && true, "children list includes newly saved asset"
                                                        );
                                                }
                                            );
                                            mgr.deleteAsset(ref.getAsset().getId(), "cleanup test node");
                                        }
                                        );
                                } else {
                                    this.resume(() => {
                                        Y.Assert.fail("Unexpected runtime failure");
                                    });
                                }
                            },
                            (err) => {
                                this.resume(
                                    () => {
                                        Y.Assert.fail("Failed save/load: " + err);
                                    }
                                );
                            }
                        );
                        this.wait(2000);
                    },

                    testUpdateListen: function () {
                        TestSuite.checkSetup();
                        var ref = TestSuite.testFolderRef;
                        var oldValue = ref.getAsset().getValue();

                        ref.addListener(
                            (ev:RefEvent) => {
                                log.log("Received update event: " + ev.eventType);
                                this.resume(() => {
                                    Y.Assert.isTrue(ev.eventType == RefEvent.EVENT_TYPES.ASSET_CHANGED,
                                                    "Listener received expected event type: " + ev.eventType
                                    );
                                    // ref should point at updated asset at this point ...
                                    var newValue = ref.getAsset().getValue();
                                    Y.Assert.isTrue(oldValue + 1 === newValue,
                                        "Asset value updated as expected: " + oldValue + " +1 ?= " + newValue
                                        );
                                });
                            }
                         );

                        mgr.saveAsset(ref.getAsset().copy().withComment("testing update at " + (new Date())
                            ).withValue(ref.getAsset().getValue() + 1).build(),
                            "small change to test update listeners"
                            );
                        
                        this.wait(1200);
                    }
                }
            ));
        return suite;
    }


}
