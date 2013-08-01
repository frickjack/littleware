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
     * HTML5 local cache
     * @class LocalCacheManager
     */
    class LocalCacheManager implements InternalManager {
        private cache: { [key: string]: InternalAssetRef; } = {};
        timestamp = 0;
        static keyPrefix = "assetManager/";

        constructor() {
            if (typeof (localStorage) != 'undefined') {
                var tsEntry = localStorage.getItem( LocalCacheManager.keyPrefix + "timestamp");
                if (tsEntry) {
                    this.timestamp = parseInt(tsEntry);
                }
            }
        }

        saveAsset(value: Asset, updateComment: string): Y.Promise {
            return this.loadAsset(value.getId()).then((refIn) => {
                var ref = <InternalAssetRef> refIn;
                this.timestamp++;
                if (value.getTimestamp() > this.timestamp) {
                    this.timestamp = value.getTimestamp() + 1;
                }

                var copy = value.copy().withTimestamp(this.timestamp).withDateUpdated(new Date()).build();

                if (typeof (localStorage) != "undefined") {
                    localStorage.setItem(LocalCacheManager.keyPrefix + copy.getId(), JSON.stringify(copy));
                    localStorage.setItem(LocalCacheManager.keyPrefix + "timestamp", "" + this.timestamp );
                }
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

        deleteAsset(id: string, deleteComment: string): Y.Promise {
            return new Y.Promise((resolve, reject) => {
                delete (this.cache[id]);
                if (typeof (localStorage) != "undefined") {
                    localStorage.removeItem(LocalCacheManager.keyPrefix + id);
                }
                resolve( id );
            });
        }

        loadAsset(id: string): Y.Promise {
            return new Y.Promise((resolve, reject) => {
                var ref = EmptyRef;
                if (this.cache[id]) {
                    ref = this.cache[id];
                    ref.refCount++;
                } else if (typeof (localStorage) != "undefined") {
                    var json = localStorage.getItem(LocalCacheManager.keyPrefix + id);
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

        getFromCache(id: string): AssetRef {
            var ref = this.cache[id];
            return ref || EmptyRef;
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
                        var a2Ref;

                        var promise = mgr.saveAsset(a2, "Saving test asset").then(
                            (ref) => {
                                Y.Assert.isTrue(a2.getId() == ref.getAsset().getId(), "Id preserved on save");
                                Y.Assert.isTrue(ref.getAsset().getTimestamp() >= 0, "Timestamp updated on save");
                                return mgr.loadAsset(a2.getId());
                            }
                        ).then(
                            (ref) => {
                                a2Ref = ref;
                                this.resume(
                                    () => {
                                        Y.Assert.isTrue(ref.isDefined(), "Able to load just saved asset");
                                        Y.Assert.isTrue(ref.getAsset().getId() == a2.getId(), "test load looks ok");
                                    }
                                );
                                return mgr.deleteAsset(ref.getAsset().getId(), "cleanup test node");
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
