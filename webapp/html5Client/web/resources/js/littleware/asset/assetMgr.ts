/// <reference path="../../libts/yui.d.ts" />
declare var exports:any;

var Y:Y = exports;

if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-manager";
}


var lw = exports.littleware;

import littleAsset = module("littleAsset");
import ax = littleAsset.littleware.asset;


/**
 * @module littleware-asset-base
 * @namespace littleware.asset
 */
export module littleware.asset.manager {
    var log = new lw.littleUtil.Logger("littleware.asset.manager");
    log.log("littleware logger loaded ...");



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
             * Id of the affected asset
             * @property id
             * @type string
             */
            public id: string,
            /**
             * Child-id is null if event-type is ASSET_CHANGED
             */
            public childId: string
            ) { }

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
        getAsset(): ax.Asset;

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
         * Decrement the reference account associated with this ref.
         * Javascript does not support "weak" references
         * (references that the garabage collector ignores).
         * @method detach
         */
        detach(): void;
    }

    /**
     * name-id tuple for AssetManager.listChildren and similar methods
     * @class NameIdPair
     */
    export class NameIdPair {
        constructor(private _name: string, private _id: string) { }

        /**
         * @method getName
         */
        getName(): string { return this._name; }
        /**
         * @method getId
         */
        getId(): string { return this._id; }
    }

    /**
     * Read-only name-id pair list suitable for re-use from cache
     * @class NameIdListRef
     */
    export class NameIdListRef {
        constructor(private _list: NameIdPair[]) { }

        /**
         * @method size
         */
        size(): number { return this._list.length; }
        /**
         * @method get
         * @param index {int}
         */
        get (index: number): NameIdPair { return this._list[index]; }

        /**
         * Copy the contents to an array that the client can manipulate
         * @method copy
         * @return {NameIdPair[]}
         */
        copy(): NameIdPair[]{
            var result: NameIdPair[] = [];
            for (var i = 0; i < this._list.length; ++i) {
                result.push(this._list[i]);
            }
            return result;
        }
    }

    /**
     * API tool for interacting with the asset repo.
     * @class AssetManager
     */
    export interface AssetManager {
        /**
         * @method addListener
         * @param listener {function}
         * @return YUI subscription with "detach" method to unsubscribe
         */
        addListener(listener: (ev: RefEvent) => void ): Y.EventHandle;

        /**
         * @method saveAsset
         * @return {Y.Promise[AssetRef]}
         */
        saveAsset(value: ax.Asset, updateComment: string): Y.Promise;
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
         * Load the asset at the given path if any - same as loadSubpath( null, path )
         * @method loadPath
         * @param path {string}
         * @return {Y.Promise{AssetRef}}
         */
        loadPath(path: string): Y.Promise;

        /**
         * Load the asset at the given path below the given root
         * @method loadSubPath
         * @param rootId {string} id of the asset to treat as the root, or null to use forest root
         * @param path {string}
         * @return {Y.Promise{AssetRef}}
         */
        loadSubpath(rootId: string, path: string): Y.Promise;

        /**
         * Like loadSubpath, but allows specification of a builder function 
         * for each segment of the path to create a missing asset if it doesn't exist -
         * buildBranch sets the builder's fromId and name properties.
         * @method buildBranch
         * @param rootId {string} id of the asset to treat as the root, or null to use forest root
         * @param branch {{name:string,builder:(parent:Asset)=>AssetBuilder}[]}
         * @return {Y.Promise{AssetRef[]}}
         */
        buildBranch(rootId: string, branch: { name: string; builder:(parent:ax.Asset) => ax.AssetBuilder; }[]): Y.Promise;


        /**
         * List the children if any under the given parent node
         * @method listChildren
         * @param parentId {string}
         * @return {Y.Promise{NameIdListRef}}
         */
        listChildren(parentId: string): Y.Promise;

        /**
         * List the root (littleware.HOME-TYPE) nodes - shortcut for listChildren(null)
         * @method listRoots
         * @return {Y.Promise{NameIdListRef}}
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
        refCount = 1;

        constructor(private asset: ax.Asset, private mgr: LocalCacheManager) {
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
         * Internal utility - resets target with the new value, and fires
         * event to listeners if 
         * value.timeStamp >= asset.timeStamp,
         * also looks for parent asset if any in local repo.
         *
         * @method update
         * @param value {Asset}
         */
        updateAsset(value: ax.Asset): void {
            var old = this.asset || null;
            Y.assert( (old === this.asset) || (value.getId() === this.asset.getId()), "Update id =? reference id");
            if ( (null == old) || (value.getTimestamp() >= old.getTimestamp()) ) {
                this.asset = value;
                //log.log("Firing update event for " + this.asset.getId());
                this.mgr.fire( new RefEvent(RefEvent.EVENT_TYPES.ASSET_CHANGED, value.getId(), null ));
                if ( (null == old) || (old.getFromId() != value.getFromId()) ) {
                    if ( old && (null != old.getFromId()) ) {
                        var oldParent = <InternalAssetRef> this.mgr.getFromCache(old.getFromId());
                        if (oldParent.isDefined()) {
                            this.mgr.fire(
                                new RefEvent(RefEvent.EVENT_TYPES.CHILD_REMOVED, oldParent.getAsset().getId(), value.getId())
                                );
                        }
                    }
                    if (null != value.getFromId()) {
                        var newParent = <InternalAssetRef> this.mgr.getFromCache(value.getFromId());
                        if (newParent.isDefined()) {
                            this.mgr.fire(
                                new RefEvent(RefEvent.EVENT_TYPES.CHILD_ADDED, newParent.getAsset().getId(), value.getId())
                                );
                        }
                    }
                } else if (null != value.getFromId()) {
                    var parent = <InternalAssetRef> this.mgr.getFromCache(value.getFromId());
                    if (parent.isDefined()) {
                        this.mgr.fire(
                            new RefEvent(RefEvent.EVENT_TYPES.CHILD_CHANGED, parent.getAsset().getId(), value.getId())
                            );
                    }
                }
            }
        }

        getAsset(): ax.Asset { return this.asset; }

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

    Y.assert(EmptyRef.isEmpty(), "empty ref better be empty!");

    export function emptyRef(): AssetRef { return EmptyRef; }

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
        private childCache: { [key: string]: NameIdListRef; } = {};
        // Only public to simplify AssetRef - CacheManager interplay
        private target: Y.EventTarget = new exports.EventTarget();

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
         * Fire an assetRefEvent for any listeners
         * @method fire
         */
        fire(ev: RefEvent): void {
            this.target.fire("assetRefEvent", <any> ev);
        }

        addListener(listener: (ev: RefEvent) => void ): Y.EventHandle {
            return this.target.on("assetRefEvent", listener);
        }

        /** 
         * Internal synchronous list children implementation - just accesses
         * cache or local storage directly.
         */
        private _listChildren(parentId: string): NameIdListRef {
            parentId = parentId || "homeRoot";
            var result: NameIdListRef = this.childCache[parentId];
            if (!result) {
                var js = JSON.parse(this.storage.getItem(LocalCacheManager.childPrefix + parentId)) || [];
                var data: NameIdPair[] = [];
                for (var i = 0; i < js.length; ++i) {
                    if (js[i]._name && js[i]._id) { // sanity check - ugh
                        data.push(new NameIdPair(js[i]._name, js[i]._id));
                    }
                }
                result = new NameIdListRef(data);
                this.childCache[parentId] = result;
            }
            return result;
        }

        /**
         * Internal save-child data 
         */
        private _saveChildren(parentId: string, data: NameIdPair[]): void {
            parentId = parentId || "homeRoot";
            // verify data
            Y.Array.each(data, (it) => {
                Y.assert(it._id && it._name, "child data looks valid");
            });

            var copy: NameIdPair[] = Y.Array.map(data, function (it) { return it; });
            if (this.childCache[parentId]) {
                // swap in new data with existing NameIdListRef - auto-updates clients that 
                // have already loaded data
                (<any> this.childCache[parentId])._list = copy;
            } else {
                this.childCache[parentId] = new NameIdListRef(copy);
            }
            var js = JSON.stringify(data);
            //log.log("Saving child info for " + parentId + ": " + js);
            this.storage.setItem(LocalCacheManager.childPrefix + parentId, js );
        }

        

        saveAsset(value: ax.Asset, updateComment: string): Y.Promise {
            return this.loadAsset(value.getId()).then((ref:InternalAssetRef) => {
               this.timestamp++;
               if (value.getTimestamp() > this.timestamp) {
                    this.timestamp = value.getTimestamp() + 1;
               }

               var copy = value.copy().withTimestamp(this.timestamp).withDateUpdated(new Date()).build();

               var oldParentId = null;
               var oldName = null;
               var newParentId = copy.getFromId();

               if (!ref.isEmpty()) {
                   //log.log("save found old data for " + copy.getName() + ": " + JSON.stringify( ref.getAsset() ) );
                   oldParentId = ref.getAsset().getFromId();
                   oldName = ref.getAsset().getName();
               }
               if (copy.getAssetType().id === ax.HomeAsset.HOME_TYPE.id) {
                   newParentId = "homeRoot";
               } else if (newParentId == null) {
                   throw new Error("Must specify asset's fromId unless it's a HOME-type asset");
               } // TODO - verify an asset has a clean path to a root (is not a child of a descendent - ugh)

               log.log("Saving asset " +copy.getName() + " (" + copy.getId() + ") under " + newParentId + ", old parent: " +oldParentId);
                //
                // update the child-lists on the old and new parent
                //
                if ((newParentId !== oldParentId) || (copy.getName() !== oldName))  // update children info
               {
                   // verify that the parent exist
                   if (copy.getAssetType().id !== ax.HomeAsset.HOME_TYPE.id) {
                       var parentRef = this._loadAsset(newParentId);
                       Y.assert(parentRef.isDefined(), "Cannot save asset under parent that does not exist" );
                   }
                    
                   // now verify that the new name or parent doesn't have a name collision with its children
                   var newParentChildren: NameIdPair[] = this._listChildren(newParentId).copy();
                   log.log("new asset's siblings: " + Y.Array.map(newParentChildren, function (it: NameIdPair) { return it.getName(); }).join(","));
                   newParentChildren = Y.Array.reject(newParentChildren, function (item: NameIdPair) { return item.getId() === copy.getId(); });
                   var twin:NameIdPair = Y.Array.find(newParentChildren, function (it: NameIdPair) { return it.getName() === copy.getName(); });
                   if (twin) {
                       throw new Error("Asset already exists under new parent with name: " + copy.getName());
                   }
                   newParentChildren.push(new NameIdPair(copy.getName(), copy.getId()));
                   this._saveChildren( newParentId, newParentChildren );

                   // update old parent if any
                   if (oldParentId && (oldParentId !== newParentId) ) {  
                       var oldParentChildren: NameIdPair[] = this._listChildren( oldParentId ).copy();
                       if (oldParentChildren.length > 0) {
                           oldParentChildren = Y.Array.reject(oldParentChildren, function (item:NameIdPair) { return item.getId() == copy.getId(); });
                           this._saveChildren( oldParentId, oldParentChildren );
                       }
                   }

               }

                // if we made it this far, then the asset passed whatever validation we have
               this.storage.setItem(LocalCacheManager.nodePrefix + copy.getId(), JSON.stringify(copy));
               this.storage.setItem(LocalCacheManager.confPrefix + "timestamp", "" + this.timestamp);

                // finally - update the in-memory cache and reference
               if ( ref.isEmpty()) {
                   ref = new InternalAssetRef(null, this);
                   this.cache[copy.getId()] = ref;
               }
               ref.updateAsset(copy);
               return ref;
            });
        }

    
        listChildren(parentId: string): Y.Promise {
           var children: NameIdListRef = this._listChildren(parentId);
           return Y.when( children );
        }

        listRoots(): Y.Promise {
            return this.listChildren( null );
        }

        _loadAsset(id: string): AssetRef {
            var ref = EmptyRef;
            if (this.cache[id]) {
                ref = this.cache[id];
                ref.refCount++;
            } else {
                var json = this.storage.getItem(LocalCacheManager.nodePrefix + id);
                //log.log("_loadAsset Loaded from local storage: " + json);
                if (json) {
                    var info = JSON.parse(json);
                    var builder = ax.AssetType.lookup(info.assetType.id).newBuilder()
                    var asset = builder.extractRaw(info).build();
                    ref = new InternalAssetRef(asset, this);
                    this.cache[id] = ref;
                }
            }
            return ref;
        }

        loadAsset(id: string): Y.Promise {
            return Y.when(this._loadAsset(id));
        }

        loadChild(parentId: string, name: string): Y.Promise {
            return this.listChildren(parentId).then(
                (siblings:NameIdListRef) => {
                    var childInfo:NameIdPair = Y.Array.find(siblings.copy(), (it:NameIdPair) => {
                        return it.getName() === name;
                    });
                    if (childInfo) {
                        return this.loadAsset(childInfo.getId() );
                    } else {
                        return Y.when(EmptyRef);
                    }
                }
            );
        }

        /*
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
                            return Y.when(ref);
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

        loadSubpath(rootId: string, path: string): Y.Promise {
            var parts = Y.Array.filter(path.split(/\/+/), (it) => { return it && true; });
            if (parts.length < 1) {
                return new Y.Promise((resolve, reject) => {
                    reject(new Error("failed to parse path: " + path));
                });
            }
            if (parts.length > 0) {
                return this._loadPath(rootId, parts);
            } else {
                return this.loadAsset(rootId);
            }
        }

        loadPath(path: string): Y.Promise {
            return this.loadSubpath(null, path);
        }


        buildBranch( rootId: string,
                     branch: { name: string; builder: (parent:ax.Asset) => ax.AssetBuilder; }[]
            ): Y.Promise {
            log.log("buildBranch: " + Y.Array.map(branch, function (it) { return it.name; } ).join( "/" ) );
            console.dir(branch);
            Y.assert((rootId) || (branch.length > 0), "nothing to do with empty rootid and empty branch");
            if (branch.length == 0) {
                return this.loadAsset(rootId);
            }
            var childInfo = branch.shift();
            return this.loadChild(rootId, childInfo.name).then(
                // create child if necessary
                (ref: AssetRef) => {
                    if ( ref.isEmpty() ) {
                        // create child if it doesn't already exist
                        var parent: ax.Asset = null;
                        if (rootId) {
                            var parentRef: AssetRef = this._loadAsset(rootId);
                            Y.assert(parentRef.isDefined(), "buildBranch root must exist");
                            parent = parentRef.getAsset();
                        }
                        return this.saveAsset(childInfo.builder(parent).withFromId(rootId).withName( childInfo.name).build(), "build test branch" );
                    } else {
                        return Y.when(ref);
                    }
                }
            ).then( 
                // child exists (or not) ... move on to next generation
                (ref: AssetRef) => {
                    if (ref.isDefined) {
                        if (branch.length == 0) {
                            return Y.when( [ref] );
                        } else {
                            return this.buildBranch(ref.getAsset().getId(), branch).then(
                                (refs: AssetRef[]) => {
                                    refs.unshift(ref);
                                    return refs;
                                }
                            );
                        }
                    } else {
                        return Y.when([ref]);
                    }
                }

            );

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
                    if (asset.getAssetType().id === ax.HomeAsset.HOME_TYPE.id) {
                        parentId = "homeRoot";
                    }
                    var siblings = this._listChildren(parentId).copy();
                    siblings = Y.Array.reject(siblings, (it:NameIdPair) => {
                        it.getId() === asset.getId();
                    });
                    this._saveChildren(parentId, siblings);
                }
            )
        }

    }

    var localMgr = new LocalCacheManager();


}
