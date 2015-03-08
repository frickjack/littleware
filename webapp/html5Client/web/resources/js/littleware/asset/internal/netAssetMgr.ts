declare var exports: any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-netmgr";
}

var lw: any = exports.littleware;

import importY = require("../../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;

import localMgrImport = require("localAssetMgr");
localMgrImport;
import localMgr = localMgrImport.littleware.asset.internal.localMgr;

import littleMgr = require("../assetMgr");
littleMgr;
import axMgr = littleMgr.littleware.asset.manager;

import littleAsset = require("../littleAsset");
littleAsset;
import ax = littleAsset.littleware.asset;

import authServiceImport = require("../../auth/authService");
authServiceImport;
import authService = authServiceImport.littleware.auth.authService;

/**
 * @module littleware-asset-localmgr
 * @namespace littleware.asset.internal.localMgr
 */
export module littleware.asset.internal.netMgr {
    var log = new lw.littleUtil.Logger("littleware.asset.internal.netMgr");
    log.log("littleware logger loaded ...");



    /**
     * Factory method to acquire the AssetManager singleton - add sesion management later
     */
    export function getNetManager():axMgr.AssetManager {
        return netMgr;
    }


    // TODO - inject this at startup/configuration time
    //var serviceRoot = "https://littleware.herokuapp.com/littleware_services/dispatch/repo";
    var serviceRoot = "http://localhost:8080/littleware_services/dispatch/repo";


    /**
     * ManagerCore implementation implements core AssetManager CRUD methods 
     * cooperating with LocalCacheManager to manage local and in-memory cache.
     * @class NetMgrCore
     */
    export class NetMgrCore implements localMgr.ManagerCore {

        constructor(
            private delegate: localMgr.LocalCacheManager,
            private authMgr: authService.AuthManager
            ) { }

        getFromCache(id: string): axMgr.AssetRef {
            return this.delegate.getFromCache(id);
        }

        fire(ev: axMgr.RefEvent): void {
            return this.delegate.fire(ev);
        }

        addListener(listener: (ev: axMgr.RefEvent) => void): Y.EventHandle {
            return this.delegate.addListener(listener);
        }


        saveAsset(value: ax.Asset, updateComment: string): Y.Promise<axMgr.AssetRef> {
            var id = ax.cleanId(value.getId());
            var payload = {
                asset: JSON.parse( JSON.stringify( value ) ),
                comment: updateComment
            };
            if (payload.asset.otherProps) {
                // sanitize payload a little bit
                log.info("Sanitizing save payload: ");
                console.dir(payload);
                payload.asset.otherProps = payload.asset.otherProps.props;
            }
            log.info("Saving payload ...");
            console.dir(payload);
            var promise = new Y.Promise(
                (resolve, reject) => {
                    var headers = this.authMgr.getIOHeaders(); 
                    headers['Content-Type'] = 'application/json';

                    Y.io(serviceRoot + "/withid/" + id, {
                        method: "PUT",
                        xdr: { credentials: true },
                        headers: headers,
                        data: JSON.stringify( payload ),
                        on: {
                            complete: (id, ev) => {
                                log.log("saveAsset response");
                                console.dir(ev);

                                if (ev.status == 200) {
                                    var jsonList = JSON.parse(ev.responseText);
                                    var assetList: ax.Asset[] = [];

                                    for (var i = 0; i < jsonList.length; ++i) {
                                        var json = jsonList[i];
                                        if (json.assetType) {
                                            var assetType: ax.AssetType = ax.AssetType.lookup(json.assetType.id, json.assetType.name);
                                            var asset: ax.Asset = assetType.newBuilder().extractRaw(json).build();
                                            assetList.push(asset);
                                        } else {
                                            log.warn("Unexpected json block in saveAsset result list");
                                            console.dir(json);
                                        }
                                    }
                                    if (assetList.length) {
                                        resolve(assetList);
                                    } else {
                                        reject(new Error("Unable to resolve assets from save result") );
                                    }
                                } else {
                                    reject(ev);
                                }
                            }
                        }
                    });
                }
                ).then(
                (assetList: ax.Asset[]) => {
                    var promises = Y.Array.map(assetList, (assetIt: ax.Asset) => {
                        return this.delegate.saveAsset(assetIt, "");
                    });
                    return Y.batch.apply(Y, promises);
                }
                ).then(
                (refList:axMgr.AssetRef[]) => {
                    var result:axMgr.AssetRef = Y.Array.find(refList, (ref: axMgr.AssetRef) => {
                        // handle case of server-assigned id to new asset
                        return ref.isDefined() && (
                            (id && (ref.getAsset().getId() == id))
                            || ((ref.getAsset().getName() == value.getName()) && (ref.getAsset().getFromId() == value.getFromId()))
                            );
                    });
                    if (result) {
                        return result;
                    } else {
                        return axMgr.emptyRef();
                    }
                }
                );

            return promise;
        }

        deleteAsset(idIn: string, deleteComment: string): Y.Promise<void> {
            var id = ax.cleanId(idIn);
            var payload = {
                comment: deleteComment
            };
            var promise = new Y.Promise(
                (resolve, reject) => {
                    var headers = this.authMgr.getIOHeaders();

                    Y.io(serviceRoot + "/withid/" + id, {
                        method: "DELETE",
                        xdr: { credentials: true },
                        headers: headers,
                        //data: JSON.stringify(payload),
                        on: {
                            complete: (id, ev) => {
                                log.log("deleteAsset response");
                                console.dir(ev);

                                if (ev.status == 200) {
                                    resolve("OK");
                                } else {
                                    reject(ev);
                                }
                            }
                        }
                    });
                }
                ).then(
                () => {
                    return this.delegate.deleteAsset(id, deleteComment);
                }
                );

            return promise;
        }


        loadAsset(idIn: string): Y.Promise<axMgr.AssetRef> {
            var id = ax.cleanId(idIn);
            Y.assert(id && true, "loadAsset given defined id");
            var promise = new Y.Promise(
                (resolve, reject) => {
                    Y.io(serviceRoot + "/withid/" + id, {
                        method: "GET",
                        xdr: { credentials: true },
                        headers: this.authMgr.getIOHeaders(),
                        on: {
                            complete: (id, ev) => {
                                log.log("loadAsset response");
                                console.dir(ev);

                                if (ev.status == 200) {
                                    var json = JSON.parse(ev.responseText);
                                    if (json.assetType) {
                                        var assetType: ax.AssetType = ax.AssetType.lookup(json.assetType.id, json.assetType.name);
                                        var asset: ax.Asset = assetType.newBuilder().extractRaw(json).build();
                                        resolve(asset);
                                    } else {
                                        reject(ev);
                                    }
                                } else {
                                    reject(ev);
                                }
                            }
                        }
                    });
                }
                ).then(
                    (asset) => {
                        return this.delegate.saveAsset(asset, "");
                    }
                );

            return promise;
        }

        loadChild(parentId: string, name: string): Y.Promise<axMgr.AssetRef> {
            // TODO - implement loadChild directly in backend ?
            log.info("netMgr loadChild listing children under " + parentId);
            var childrenPromise = parentId ? this.listChildren(parentId) : this.listRoots();
            return childrenPromise.then(
                (listRef: axMgr.NameIdListRef) => {
                    log.info("loadChild searching child list");
                    return listRef.find(
                        (pair: axMgr.NameIdPair) => { return (pair.getName() == name); }
                        );
                }
                ).then(
                (pair:axMgr.NameIdPair) => {
                    if (pair) {
                        log.info("loadChild loading found child");
                        return this.loadAsset(pair.getId());
                    } else {
                        log.info("loadChild returning empty ref");
                        return axMgr.emptyRef();
                    }
                }
                );
        }

        listChildren(parentIdIn: string): Y.Promise<axMgr.NameIdListRef> {
            var parentId = ax.cleanId(parentIdIn);
            var promise = new Y.Promise(
                (resolve, reject) => {
                    Y.io(serviceRoot + "/childrenof/" + parentId, {
                        method: "GET",
                        xdr: { credentials: true },
                        headers: this.authMgr.getIOHeaders(),
                        on: {
                            complete: (id, ev) => {
                                log.log("listChildren response");
                                console.dir(ev);
                                var result: axMgr.NameIdListRef;

                                if (ev.status == 200) {
                                    var json = JSON.parse(ev.responseText);
                                    var data: axMgr.NameIdPair[] = [];
                                    var key;
                                    for (key in json) {
                                        if (key != "littleStatus") {
                                            var obj = json[key];
                                            data.push(new axMgr.NameIdPair(obj.name, obj.id));
                                        }
                                    }
                                    result = this.delegate.saveChildren(parentId, data);
                                    resolve(result);
                                } else if (ev.status == 404) {
                                    result = this.delegate.saveChildren(parentId, []);
                                    resolve(result);
                                } else {
                                    reject(ev);
                                }
                            }
                        }
                    });
                }
                );

            return promise;
        }

        listRoots(): Y.Promise<axMgr.NameIdListRef> {
            var promise = new Y.Promise(
                (resolve, reject) => {
                    Y.io(serviceRoot + "/roots", {
                        method: "GET",
                        xdr: { credentials: true },
                        headers: this.authMgr.getIOHeaders(),
                        on: {
                            complete: (id, ev) => {
                                log.log("listRoots response");
                                console.dir(ev);

                                if (ev.status == 200) {
                                    var json = JSON.parse(ev.responseText);
                                    var data: axMgr.NameIdPair[] = [];
                                    var key;
                                    for (key in json) {
                                        if (key != "littleStatus") {
                                            var obj = json[key];
                                            data.push(new axMgr.NameIdPair(obj.name, obj.id));
                                        }
                                    }
                                    var result: axMgr.NameIdListRef = this.delegate.saveChildren(null, data);
                                    resolve(result);
                                } else {
                                    reject(ev);
                                }
                            }
                        }
                    });
                }
                );

            return promise;
        }

    }

    //----------------------------------

    var netMgr: axMgr.AssetManager = new localMgr.SimpleManager(
        new NetMgrCore( new localMgr.LocalCacheManager(), authService.Factory.get() )
        );



}
