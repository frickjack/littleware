declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-asset-test";
}

import importY = require("../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;

import importLittleAsset = require("littleAsset");
importLittleAsset;
import ax = importLittleAsset.littleware.asset;

import importAssetMgr = require("assetMgr");
importAssetMgr;
import axMgr = importAssetMgr.littleware.asset.manager;

import importAuth = require("../auth/authService");
importAuth;
import authService = importAuth.littleware.auth.authService;


// hook into littleware javascript coded modules ...
var lw: any = exports.littleware;


/**
 * @module littleware-asset-base
 * @namespace littleware.asset
 */
export module littleware.asset.test {
    var log = new lw.littleUtil.Logger("littleware.asset.test");
    log.log("littleware logger loaded ...");


    module TestSuite {
        export var testHomeId = "D589EABED8EA43C1890DBF3CF1F9689A";
        //export var homeRef: axMgr.AssetRef;
        export var folderId = "63E182C6E64C4C598A0B5FAB6597B54A";
        export var testFolderRef: axMgr.AssetRef;
        export var mgr = axMgr.getAssetManager();


        var authMgr: authService.AuthManager = authService.Factory.get();

        export var setupPromise = authMgr.authenticate("littleware.test_user", "testSecret").then(
            () => {
                log.info("setupPromise: Authenticated as test user, building test branch ..." );
                return mgr.buildBranch(null, [
                    {
                        name: "littleware.test_home",
                        builder: (parent: ax.Asset) => {
                            return ax.HomeAsset.HOME_TYPE.newBuilder().withId(testHomeId
                                ).withComment("root of test sandbox node tree");
                        }
                    },
                    {
                        name: "testFolder",
                        builder: (parent: ax.Asset) => {
                            return ax.GenericAsset.GENERIC_TYPE.newBuilder(
                                ).withComment("parent asset for simple build test"
                                ).withParent(parent);
                        }
                    }
                ]).then(
                    (refs: axMgr.AssetRef[]) => {
                        log.log("Test setup looks ok ...");
                        testFolderRef = refs[1];
                    },
                    (error) => {
                        log.log("Ugh! setup failed: " + error);
                        console.dir(error);
                    }
                    );
            });


        /**
         * Little helper - verifies that test environment has been setup
         * @method checkSetup
         * @param testCase {TestCase}
         */
        export function checkSetup():void {
            Y.Assert.isTrue( testFolderRef && true, 
                "littleware.test_home setup properly - folderRef: " + testFolderRef
                );
        }
    }

    // raw json from http://localhost:8080/littleware_services/dispatch/repo/withId/bd46e558-8f9d-4f41-a631-0100fe68dcb4
    // for extractRaw test case below
    var rawJsonData = {
        "type": "littleware.HOME",
        "name": "littleware.home",
        "id": "bd46e558-8f9d-4f41-a631-0100fe68dcb4",
        "homeId": "bd46e558-8f9d-4f41-a631-0100fe68dcb4",
        "aclId": "f4cedaa0-7b57-4fff-a27e-0ba87078dc34",
        "ownerId": "00000000-0000-0000-0000-000000000000",
        "creatorId": "00000000-0000-0000-0000-000000000000",
        "dateCreated": "2013-06-14 11:54:41",
        "updaterId": "00000000-0000-0000-0000-000000000000",
        "dateUpdated": "2013-06-14 11:54:41",
        "updateComment": "auto-created by AwsModule domain setup",
        "timestamp": "100",
        "comment": "littleware home tree",
        "value": 0.0,
        "state": 0,
        "data": "",
        "otherProps": {}, "linkMap": {}, "dateMap": {}, "littleStatus": 200
    };

    export function buildTestSuite(): Y.Test_TestSuite {
        var suite: Y.Test_TestSuite = new Y.Test.TestSuite("littleware-asset");
        var mgr = axMgr.getAssetManager();

        suite.add( new Y.Test.TestCase(
            {
                name: "Test Asset Stuff",

                testIdFactory: function () {
                    var factory = ax.IdFactories.get();
                    var id1 = factory.get();
                    var id2 = factory.get();
                    Y.Assert.isTrue(id1 != id2, "Id factory generating different ids: " + id1 + " ?= " + id2);
                },

                testSetup: function () {
                    TestSuite.setupPromise.then(
                        () => {
                            this.resume();
                        },
                        (err) => {
                            log.warn("testSetup failed");
                            console.dir(err);
                            log.warn( "-------------------------" );
                            Y.Assert.fail("Setup failed");
                        }
                     );
                    this.wait(5000);
                } 
                
                ,

                testAssetBuild: function () {
                    TestSuite.checkSetup();
                    var parent = TestSuite.testFolderRef.getAsset();
                    var a1 = ax.GenericAsset.GENERIC_TYPE.newBuilder().withName("a1"
                        ).withComment("child of parent").withParent(parent
                        ).build();
                    var a1Copy = a1.copy().build();
                    Y.Assert.isTrue(a1.getName() == "a1", "asset build set expected name: " + a1.getName());
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
                    var a1: ax.Asset = this.testAssetBuild();
                    // make name unique
                    var a2 = a1.copy().withName("a1" + (new Date()).getTime()).build();
                    var a2Path = "/littleware.test_home/testFolder/" + a2.getName();

                    var promise = mgr.saveAsset(a2, "Saving test asset").then(
                        (ref:axMgr.AssetRef) => {
                            Y.Assert.isTrue(ref.isDefined(), "saveAsset returns reference");
                            Y.Assert.isTrue(ref.getAsset().getId() && true, "Id preserved on save");
                            Y.Assert.isTrue(ref.getAsset().getTimestamp() >= 0, "Timestamp updated on save");
                            a2 = ref.getAsset();
                            return Y.batch(
                                mgr.loadAsset(a2.getId()),
                                mgr.listChildren(a2.getFromId()),
                                mgr.listRoots(),
                                mgr.loadChild(null, "littleware.test_home"),
                                mgr.loadChild(a2.getFromId(), a2.getName()),
                                mgr.loadPath(a2Path)
                                );
                        }
                        ).then(
                        (batchVec) => {
                            var ref: axMgr.AssetRef, childList: axMgr.NameIdListRef, rootList: axMgr.NameIdListRef, homeRef: axMgr.AssetRef, nameRef: axMgr.AssetRef, pathRef: axMgr.AssetRef;
                            if (batchVec.length > 1) {
                                ref = batchVec[0];
                                childList = batchVec[1];
                                rootList = batchVec[2];
                                homeRef = batchVec[3];
                                nameRef = batchVec[4];
                                pathRef = batchVec[5];

                                log.log("Attempting to save asset with same name under test parent");
                                // finally - try to save an asset with the same name - should fail
                                mgr.saveAsset(ref.getAsset().copy().withId(null).build(),
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
                                                Y.Assert.isTrue(rootList.size() > 0, "Non-empty root list");
                                                Y.Assert.isTrue(homeRef.isDefined() && (homeRef.getAsset().getName() == "littleware.test_home"), "Loaded test home");
                                                Y.Assert.isTrue(nameRef.isDefined() && (nameRef.getAsset().getId() == asset.getId()),
                                                    "loadChild works ..."
                                                    );
                                                Y.Assert.isTrue(pathRef.isDefined() && (pathRef.getAsset().getId() == asset.getId()),
                                                    "loadPath works ...: " + a2Path
                                                    );
                                                Y.Assert.isTrue(asset.getId() == a2.getId(), "test load looks ok");
                                                Y.Assert.isTrue(childList.size() > 0, "test child list non empty");
                                                Y.Assert.isTrue(Y.Array.find(childList.copy(),
                                                    function (it: axMgr.NameIdPair) { return (it.getId() == asset.getId()) && (it.getName() == asset.getName()); }
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
                                    console.dir(err);
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

                    mgr.addListener(
                        (ev: axMgr.RefEvent) => {
                            log.log("Received update event: " + ev.eventType);
                            this.resume(() => {
                                Y.Assert.isTrue(ev.eventType == axMgr.RefEvent.EVENT_TYPES.ASSET_CHANGED,
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
                },

                testRoots: function () {
                    TestSuite.setupPromise.then(
                            () => {
                                return mgr.listRoots();
                            }
                        ).then(
                        (rootList: axMgr.NameIdListRef) => {
                            rootList.each(
                                (idPair: axMgr.NameIdPair) => {
                                    log.info("Root pair: " + idPair.getId() + " - " + idPair.getName());
                                }
                                );
                            this.resume(() => {
                                Y.Assert.isTrue(
                                    rootList.size() > 0, "list roots non-empty"
                                    );
                            });
                        },
                        (err) => {
                            this.resume(() => {
                                log.info("listRoots failed");
                                console.dir(err);
                                Y.Assert.fail("listRoots failed");
                            });
                        }
                        );
                    this.wait(5000);
                },    

                testExtractRaw: function () {
                    var atype: ax.AssetType = ax.AssetType.lookup(rawJsonData.type, "unknown" );
                }

            }
                
            ));

        return suite;
    }


}
