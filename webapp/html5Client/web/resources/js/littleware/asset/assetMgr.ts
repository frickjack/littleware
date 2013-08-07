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
        getAsset(): ax.Asset;

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

        constructor(private asset: ax.Asset, private mgr: InternalManager) {
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
        updateAsset(value: ax.Asset): void {
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

        getAsset(): ax.Asset { return this.asset; }

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

        

        saveAsset(value: ax.Asset, updateComment: string): Y.Promise {
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
               if (copy.getAssetType().id === ax.HomeAsset.HOME_TYPE.id) {
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
                        var builder = ax.AssetType.lookup(info.assetType.id).newBuilder()
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
                    if (asset.getAssetType().id === ax.HomeAsset.HOME_TYPE.id) {
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


}
