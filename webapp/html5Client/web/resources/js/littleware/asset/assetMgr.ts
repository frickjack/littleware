declare var exports: any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-manager";
}

var lw: any = exports.littleware;

import importY = require("../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;

import littleAsset = require("littleAsset");
littleAsset;
import ax = littleAsset.littleware.asset;


/**
 * @module littleware-asset-manager
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
        isEmpty(): boolean;

        /**
         * ! isEmpty
         * @method isDefined
         */
        isDefined(): boolean;

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
        private _id: string;

        constructor(private _name: string, idIn: string) {
            this._id = ax.cleanId(idIn);
        }

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

        /**
         * list comprehension ...
         * @method each
         * @param thunk {(NameIdPair) => any}
         */
        each(thunk: (it: NameIdPair) => any): void {
            Y.Array.each(this._list, thunk);
        }

        /**
         * list comprehension ...
         * @method map
         * @param thunk {(NameIdPair) => T}
         * @return {T[]}
         */
        map<T>(thunk: (it: NameIdPair) => T): T[] {
            return Y.Array.map(this._list, thunk);
        }

        /**
         * list comprehension ...
         * @method filter
         * @param thunk {(NameIdPair) => boolean}
         * @return {NameIdPair[]}
         */
        filter(thunk: (it: NameIdPair) => boolean): NameIdPair[]{
            return Y.Array.filter(this._list, thunk);
        }

        /**
         * list comprehension ...
         * @method find
         * @param thunk {(NameIdPair) => boolean}
         * @return {NameIdPair}
         */
        find(thunk: (it: NameIdPair) => boolean): NameIdPair {
            return Y.Array.find(this._list, thunk);
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
        saveAsset(value: ax.Asset, updateComment: string): Y.Promise<AssetRef>;

        /**
         * @method deleteAsset
         * @return {Y.Promise[void]}
         */
        deleteAsset(id: string, deleteComment: string): Y.Promise<void>;

        /**
         * Return a reference to the asset with the given id if any - otherwise null.
         * @method loadAsset
         * @return {Y.Promise[AssetRef]}
         */
        loadAsset(id: string): Y.Promise<AssetRef>;

        /**
         * Load the child of the given parent with the given name -
         * every child has a unique name within its set of siblings.
         * Usually implemented as a shortcut for loadAsset( listChildren( parentId )[name] ).
         * @method loadChild
         * @param parentId {string}
         * @param name {string}
         * @return {Y.Promise{AssetRef}}
         */
        loadChild(parentId: string, name: string): Y.Promise<AssetRef>;

        /**
         * List the children if any under the given parent node
         * @method listChildren
         * @param parentId {string}
         * @return {Y.Promise{NameIdListRef}}
         */
        listChildren(parentId: string): Y.Promise<NameIdListRef>;

        /**
         * List the root (littleware.HOME-TYPE) nodes - shortcut for listChildren(null)
         * @method listRoots
         * @return {Y.Promise{NameIdListRef}}
         */
        listRoots(): Y.Promise<NameIdListRef>;

        /**
         * Load the asset at the given path if any - same as loadSubpath( null, path )
         * @method loadPath
         * @param path {string}
         * @return {Y.Promise{AssetRef}}
         */
        loadPath(path: string): Y.Promise<AssetRef>;

        /**
         * Load the asset at the given path below the given root
         * @method loadSubPath
         * @param rootId {string} id of the asset to treat as the root, or null to use forest root
         * @param path {string}
         * @return {Y.Promise{AssetRef}}
         */
        loadSubpath(rootId: string, path: string): Y.Promise<AssetRef>;

        /**
         * Like loadSubpath, but allows specification of a builder function 
         * for each segment of the path to create a missing asset if it doesn't exist -
         * buildBranch sets the builder's fromId and name properties.
         * @method buildBranch
         * @param rootId {string} id of the asset to treat as the root, or null to use forest root
         * @param branch {{name:string,builder:(parent:Asset)=>AssetBuilder}[]}
         * @return {Y.Promise{AssetRef[]}}
         */
        buildBranch(rootId: string, branch: { name: string; builder: (parent: ax.Asset) => ax.AssetBuilder; }[]): Y.Promise<AssetRef[]>;

    }

    /**
     * Factory method to acquire the AssetManager singleton - add sesion management later
     * @method getAssetManager
     * @static
     * @for littleware.asset.manager
     */
    export function getAssetManager(): AssetManager {
        //
        // slightly goofy implementation to avoid circular import with internal module -
        // might be better to just import the internal module, but whatever
        //
        return lw.asset.internal.netMgr.getNetManager();
    }



    /**
     * Factory method to acquire an empty AssetRef
     * @method emptyRef
     * @for littleware.asset.manager
     * @static
     */
    export function emptyRef(): AssetRef {
        //
        // slightly goofy implementation to avoid circular import with internal module -
        // might be better to just import the internal module, but whatever
        //
        return lw.asset.internal.localMgr.emptyRef();
    }



}
