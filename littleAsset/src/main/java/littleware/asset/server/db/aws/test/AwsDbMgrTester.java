/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.TreeNode;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.server.db.test.DbAssetManagerTester;

/**
 * Run the AwsDbAssetManager through some tests
 */
public class AwsDbMgrTester extends DbAssetManagerTester {
    @Inject
    public AwsDbMgrTester(DbAssetManager dbMgr,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            LittleTransaction trans) {
        super(dbMgr, nodeProvider, trans);
    }
    
    @Override
    public void setUp() {
        
    }
}
