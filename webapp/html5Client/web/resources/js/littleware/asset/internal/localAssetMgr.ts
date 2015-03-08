declare var exports: any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-localmgr";
}

var lw: any = exports.littleware;

import importY = require("../../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;

import littleMgr = require("../assetMgr");
littleMgr;
import axMgr = littleMgr.littleware.asset.manager;

import littleAsset = require("../littleAsset");
littleAsset;
import ax = littleAsset.littleware.asset;


/**
 * @module littleware-asset-localmgr
 * @namespace littleware.asset.internal.localMgr
 */
export module littleware.asset.internal.localMgr {
    var log = new lw.littleUtil.Logger("littleware.asset.internal.localMgr");
    log.log("littleware logger loaded ...");



    /**
     * Factory method to acquire the AssetManager singleton - add sesion management later
     */
    export function getLocalManager():axMgr.AssetManager {
        return localMgr;
    }



    /**
     * Internal interface extends AssetManager 
     * with a getFromCache() method for accessing AssetRefs that
     * have already been loaded into memory by the app for triggering
     * update events on listeners and that kind of thing.
     *
     * @class EventHelper
     */
    export interface EventHelper {
        /**
         * For accessing AssetRefs that
         * have already been loaded into memory by the app for triggering
         * update events on listeners and that kind of thing.
         *
         * @method getFromCache
         * @param id {string}
         * @return {AssetRef}
         */
        getFromCache(id: string): axMgr.AssetRef;

        /**
         * Fire an assetRefEvent for any listeners
         * @method fire
         */
        fire(ev: axMgr.RefEvent): void;
    }


    /**
     * Internal implementation of AssetRef
     * @class InternalAssetRef
     */
    export class InternalAssetRef implements axMgr.AssetRef {
        refCount = 1;

        constructor(private asset: ax.Asset, private mgr: EventHelper) {
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
            if ( (null == old) || (value.getTimestamp() > old.getTimestamp()) ) {
                this.asset = value;
                //log.log("Firing update event for " + this.asset.getId());
                this.mgr.fire(new axMgr.RefEvent(axMgr.RefEvent.EVENT_TYPES.ASSET_CHANGED, value.getId(), null ));
                if ( (null == old) || (old.getFromId() != value.getFromId()) ) {
                    if ( old && (null != old.getFromId()) ) {
                        var oldParent = <InternalAssetRef> this.mgr.getFromCache(old.getFromId());
                        if (oldParent.isDefined()) {
                            this.mgr.fire(
                                new axMgr.RefEvent(axMgr.RefEvent.EVENT_TYPES.CHILD_REMOVED, oldParent.getAsset().getId(), value.getId())
                                );
                        }
                    }
                    if (null != value.getFromId()) {
                        var newParent = <InternalAssetRef> this.mgr.getFromCache(value.getFromId());
                        if (newParent.isDefined()) {
                            this.mgr.fire(
                                new axMgr.RefEvent(axMgr.RefEvent.EVENT_TYPES.CHILD_ADDED, newParent.getAsset().getId(), value.getId())
                                );
                        }
                    }
                } else if (null != value.getFromId()) {
                    var parent = <InternalAssetRef> this.mgr.getFromCache(value.getFromId());
                    if (parent.isDefined()) {
                        this.mgr.fire(
                            new axMgr.RefEvent(axMgr.RefEvent.EVENT_TYPES.CHILD_CHANGED, parent.getAsset().getId(), value.getId())
                            );
                    }
                }
            }
        }

        getAsset(): ax.Asset { return this.asset; }

        isEmpty(): boolean { return this.asset == null; }

        isDefined(): boolean { return !this.isEmpty(); }

        map(lambda: (Asset) => any): any {
            if (this.isEmpty()) {
                return null;
            }
            return lambda(this.asset);
        }

    }

    var EmptyRef = new InternalAssetRef(null, null);

    Y.assert(EmptyRef.isEmpty(), "empty ref better be empty!");

    export function emptyRef(): axMgr.AssetRef { return EmptyRef; }

    //Y.augment( <any> AssetRef, <any> Y.EventTarget);

    //-----------------------------------

    /**
     * Core AssetManager CRUD methods -
     * the other methods build on these.
     * SimpleManager delegates to a ManagerCore implementation.
     * @class ManagerCore
     */
    export interface ManagerCore extends EventHelper {
        addListener(listener: (ev: axMgr.RefEvent) => void): Y.EventHandle;

        saveAsset(value: ax.Asset, updateComment: string): Y.Promise<axMgr.AssetRef>;
        
        deleteAsset(id: string, deleteComment: string): Y.Promise<void>;

        loadAsset(id: string): Y.Promise<axMgr.AssetRef>;

        loadChild(parentId: string, name: string): Y.Promise<axMgr.AssetRef>;

        listChildren(parentId: string): Y.Promise<axMgr.NameIdListRef>;

        listRoots(): Y.Promise<axMgr.NameIdListRef>;

    }

    //----------------------------------


    /**
     * Manager implementation that stores its repository in the 
     * HTML5 local cache.  Note - 5MB storage limit in most browsers.
     * @class LocalCacheManager
     */
    export class LocalCacheManager implements ManagerCore {
        //
        // TODO - eventually will need to eject old data from in-memory cache (check littleUtil.Cache),
        //  but just keep everything in memory for now - just building little toy apps
        //
        private cache: { [key: string]: InternalAssetRef; } = {};
        private childCache: { [key: string]: axMgr.NameIdListRef; } = {};
        private target: Y.EventTarget = new exports.EventTarget();

        timestamp = 0;
        static nodePrefix = "assetMan/nodes/";
        static childPrefix = "assetMan/children/";
        static confPrefix = "assetMan/conf/";

        // alias for localStorage - see constructor
        private storage = {
            getItem: function (key: string): string { return null; },
            setItem: function (key: string, value: string): void { },
            removeItem: function (key: string): void { }
        };

        constructor() {
            if (typeof (localStorage) != 'undefined') {
                this.storage = localStorage;
                var tsEntry = this.storage.getItem(LocalCacheManager.confPrefix + "timestamp");
                if (tsEntry) {
                    this.timestamp = parseInt(tsEntry);
                }
            }
        }


        /**
         * Fire an assetRefEvent for any listeners
         * @method fire
         */
        fire(ev: axMgr.RefEvent): void {
            this.target.fire("assetRefEvent", <any> ev);
        }

        addListener(listener: (ev: axMgr.RefEvent) => void): Y.EventHandle {
            return this.target.on("assetRefEvent", listener);
        }

        /** 
         * Internal synchronous list children implementation - just accesses
         * cache or local storage directly.
         */
        private _listChildren(parentId: string): axMgr.NameIdListRef {
            parentId = parentId || "homeRoot";
            var result: axMgr.NameIdListRef = this.childCache[parentId];
            if (!result) {
                var js = JSON.parse(this.storage.getItem(LocalCacheManager.childPrefix + parentId)) || [];
                var data: axMgr.NameIdPair[] = [];
                for (var i = 0; i < js.length; ++i) {
                    if (js[i]._name && js[i]._id) { // sanity check - ugh
                        data.push(new axMgr.NameIdPair(js[i]._name, js[i]._id));
                    }
                }
                result = new axMgr.NameIdListRef(data);
                this.childCache[parentId] = result;
            }
            return result;
        }

        /**
         * Internal save-child data.  Not private to allow access by NetAssetMgr.
         */
        saveChildren(parentId: string, data: axMgr.NameIdPair[]): axMgr.NameIdListRef {
            parentId = parentId || "homeRoot";
            // verify data
            Y.Array.each(data, (it) => {
                Y.assert(it._id && it._name, "child data looks valid");
            });

            var copy: axMgr.NameIdPair[] = Y.Array.map(data, function (it) { return it; });
            if (this.childCache[parentId]) {
                // swap in new data with existing NameIdListRef - auto-updates clients that 
                // have already loaded data
                (<any> this.childCache[parentId])._list = copy;
            } else {
                this.childCache[parentId] = new axMgr.NameIdListRef(copy);
            }
            var js = JSON.stringify(data);
            //log.log("Saving child info for " + parentId + ": " + js);
            this.storage.setItem(LocalCacheManager.childPrefix + parentId, js);
            return this.childCache[parentId];
        }



        saveAsset(valueIn: ax.Asset, updateComment: string): Y.Promise<axMgr.AssetRef> {
            var value: ax.Asset = valueIn;
            if (!value.getId()) {
                // a newly created asset may need an id assigned to it
                value = value.copy().withId(ax.IdFactories.get().get()).build();
            }

            var promise = new Y.Promise<axMgr.AssetRef>(
                (resolve, reject) => {
                    // extract out helper function that runs whether loadAsset succeeds or fails ...
                    var helper = (ref: InternalAssetRef) => {
                        this.timestamp++;
                        if (value.getTimestamp() > this.timestamp) {
                            this.timestamp = value.getTimestamp() + 1;
                        }

                        log.info("saveAsset making copy for cache");
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

                        log.log("Saving asset " + copy.getName() + " (" + copy.getId() + ") under " + newParentId + ", old parent: " + oldParentId);
                        //
                        // update the child-lists on the old and new parent
                        //
                        if ((newParentId !== oldParentId) || (copy.getName() !== oldName))  // update children info
                        {
                            // verify that the parent exist
                            if (copy.getAssetType().id !== ax.HomeAsset.HOME_TYPE.id) {
                                var parentRef = this._loadAsset(newParentId);
                                Y.assert(parentRef.isDefined(), "Cannot save asset under parent that does not exist");
                            }

                            // now verify that the new name or parent doesn't have a name collision with its children
                            var newParentChildren: axMgr.NameIdPair[] = this._listChildren(newParentId).copy();
                            log.log("new asset's siblings: " + Y.Array.map(newParentChildren, function (it: axMgr.NameIdPair) { return it.getName(); }).join(","));
                            newParentChildren = Y.Array.reject(newParentChildren, function (item: axMgr.NameIdPair) { return item.getId() === copy.getId(); });
                            var twin: axMgr.NameIdPair = Y.Array.find(newParentChildren, function (it: axMgr.NameIdPair) { return it.getName() === copy.getName(); });
                            if (twin) {
                                log.warn("throwing error - invalid twin asset ...");
                                throw new Error("Asset already exists under new parent with name: " + copy.getName());
                            }
                            newParentChildren.push(new axMgr.NameIdPair(copy.getName(), copy.getId()));
                            log.info("saveAsset updating child cache");
                            this.saveChildren(newParentId, newParentChildren);

                            // update old parent if any
                            if (oldParentId && (oldParentId !== newParentId)) {
                                var oldParentChildren: axMgr.NameIdPair[] = this._listChildren(oldParentId).copy();
                                if (oldParentChildren.length > 0) {
                                    oldParentChildren = Y.Array.reject(oldParentChildren, function (item: axMgr.NameIdPair) { return item.getId() == copy.getId(); });
                                    this.saveChildren(oldParentId, oldParentChildren);
                                }
                            }

                        }

                        // if we made it this far, then the asset passed whatever validation we have
                        this.storage.setItem(LocalCacheManager.nodePrefix + copy.getId(), JSON.stringify(copy));
                        this.storage.setItem(LocalCacheManager.confPrefix + "timestamp", "" + this.timestamp);

                        // finally - update the in-memory cache and reference
                        if (ref.isEmpty()) {
                            ref = new InternalAssetRef(null, this);
                            this.cache[copy.getId()] = ref;
                        }
                        ref.updateAsset(copy);
                        log.info("saveAsset returning result");
                        console.dir(ref);
                        resolve(ref);
                    };


                    this.loadAsset(value.getId()).then(helper,
                        (err) => {
                            log.info("loadAsset failed with error");
                            console.dir(err);
                            helper( EmptyRef );
                        });
                });
            return promise;
        }


        listChildren(parentId: string): Y.Promise<axMgr.NameIdListRef> {
            var children: axMgr.NameIdListRef = this._listChildren(parentId);
            return Y.when(children);
        }

        listRoots(): Y.Promise<axMgr.NameIdListRef> {
            return this.listChildren(null);
        }

        _loadAsset(id: string): axMgr.AssetRef {
            var ref = EmptyRef;
            if (this.cache[id]) {
                ref = this.cache[id];
                ref.refCount++;
            } else {
                var json = this.storage.getItem(LocalCacheManager.nodePrefix + id);
                //log.log("_loadAsset Loaded from local storage: " + json);
                if (json) {
                    var info = JSON.parse(json);
                    var builder = ax.AssetType.lookup(info.assetType.id).newBuilder();
                    // new home-id property - just push old assets under test-home for now
                    info.homeId = info.homeId || "D589EABED8EA43C1890DBF3CF1F9689A";
                    var asset = builder.extractRaw(info).build();
                    ref = new InternalAssetRef(asset, this);
                    this.cache[id] = ref;
                }
            }
            return ref;
        }

        loadAsset(id: string): Y.Promise<axMgr.AssetRef> {
            return Y.when(this._loadAsset(id));
        }

        loadChild(parentId: string, name: string): Y.Promise<axMgr.AssetRef> {
            return this.listChildren(parentId).then(
                (siblings: axMgr.NameIdListRef) => {
                    var childInfo: axMgr.NameIdPair = Y.Array.find(siblings.copy(), (it: axMgr.NameIdPair) => {
                        return it.getName() === name;
                    });
                    if (childInfo) {
                        return this.loadAsset(childInfo.getId());
                    } else {
                        return Y.when(EmptyRef);
                    }
                }
                );
        }


        getFromCache(id: string): axMgr.AssetRef {
            var ref = this.cache[id];
            return ref || EmptyRef;
        }

        deleteAsset(id: string, deleteComment: string): Y.Promise<void> {
            return Y.Promise.batch(
                this.loadAsset(id), this.listChildren(id)
                ).then(
                (dataVec) => {
                    var ref: axMgr.AssetRef = dataVec[0];
                    var childList: axMgr.NameIdPair[] = dataVec[1];
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
                    siblings = Y.Array.reject(siblings, (it: axMgr.NameIdPair) => {
                        return it.getId() === asset.getId();
                    });
                    this.saveChildren(parentId, siblings);
                }
                )
        }


    }

        //---------------------


    /**
     * AssetManager implementation delegates most methods to an injected ManagerCore
     * @class SimpleManager
     */
    export class SimpleManager implements axMgr.AssetManager {

        constructor(private core: ManagerCore) { }

        saveAsset(value: ax.Asset, updateComment: string): Y.Promise<axMgr.AssetRef> {
            return this.core.saveAsset(value, updateComment);
        }

        deleteAsset(id: string, deleteComment: string): Y.Promise<void> {
            return this.core.deleteAsset(id, deleteComment);
        }

        loadAsset(id: string): Y.Promise<axMgr.AssetRef> {
            return this.core.loadAsset(id);
        }

        loadChild(parentId: string, name: string): Y.Promise<axMgr.AssetRef> {
            return this.core.loadChild(parentId, name);
        }

        listChildren(parentId: string): Y.Promise<axMgr.NameIdListRef> {
            return this.core.listChildren(parentId);
        }

        listRoots(): Y.Promise<axMgr.NameIdListRef> {
            return this.core.listRoots();
        }

        getFromCache(id: string): axMgr.AssetRef {
            return this.core.getFromCache(id);
        }

        fire(ev: axMgr.RefEvent): void {
            return this.core.fire(ev);
        }

        addListener( listener: (ev: axMgr.RefEvent) => void ): Y.EventHandle {
            return this.core.addListener( listener );
        }

        /*
         * Internal helper - traverse path recursively
         */
        private _loadPath(parentId: string, partsLeft: string[]): Y.Promise<axMgr.AssetRef> {
            var name = partsLeft.shift();
            //log.log("_loadPath loading " + parentId + " child " + name);
            var childPromise = this.core.loadChild(parentId, name);
            if (partsLeft.length > 0) {
                return childPromise.then(
                    (ref: axMgr.AssetRef) => {
                        if (ref.isEmpty()) {
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


        loadSubpath(rootId: string, path: string): Y.Promise<axMgr.AssetRef> {
            var parts = Y.Array.filter(path.split(/\/+/), (it) => { return it && true; });
            if (parts.length < 1) {
                return new Y.Promise<axMgr.AssetRef>((resolve, reject) => {
                    reject(new Error("failed to parse path: " + path));
                });
            }
            if (parts.length > 0) {
                return this._loadPath(rootId, parts);
            } else {
                return this.core.loadAsset(rootId);
            }
        }

        loadPath(path: string): Y.Promise<axMgr.AssetRef> {
            return this.loadSubpath(null, path);
        }


        buildBranch( rootId: string,
                     branch: { name: string; builder: (parent:ax.Asset) => ax.AssetBuilder; }[]
            ): Y.Promise<axMgr.AssetRef[]> {
            log.log("buildBranch: " + Y.Array.map(branch, function (it) { return it.name; } ).join( "/" ) );
            console.dir(branch);
            Y.assert((rootId) || (branch.length > 0), "nothing to do with empty rootid and empty branch");
            if (branch.length == 0) {
                return this.core.loadAsset(rootId);
            }
            var childInfo = branch.shift();
            return this.core.loadChild(rootId, childInfo.name).then(
                // create child if necessary
                (ref: axMgr.AssetRef) => {
                    log.info("buildBranch loaded child: ");
                    console.dir(ref);
                    if ( ref.isEmpty() ) {
                        // create child if it doesn't already exist
                        var parentPromise: Y.Promise<ax.Asset> = Y.when(null);
                        if (rootId) {
                            parentPromise = this.loadAsset(rootId).then(
                                (parentRef:axMgr.AssetRef) => {
                                    Y.assert(parentRef.isDefined(), "buildBranch root must exist");
                                    return parentRef.getAsset();
                                }
                             );
                        }
                        return parentPromise.then(
                            (parent:ax.Asset) => {
                                return this.core.saveAsset(
                                    childInfo.builder(parent).withHomeId( parent.getHomeId() || rootId ).withFromId(rootId).withName(childInfo.name).build(),
                                    "build test branch"
                                    );
                            }
                         );
                    } else {
                        return Y.when(ref);
                    }
                }
            ).then( 
                // child exists (or not) ... move on to next generation
                (ref: axMgr.AssetRef) => {
                        if (ref.isDefined) {
                            if (branch.length == 0) {
                                return Y.when( [ref] );
                            } else {
                                return this.buildBranch(ref.getAsset().getId(), branch).then(
                                    (refs: axMgr.AssetRef[]) => {
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
    }

    var localMgr: axMgr.AssetManager = new SimpleManager(new LocalCacheManager());

}
