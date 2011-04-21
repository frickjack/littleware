/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.tracker.Comment;
import littleware.apps.tracker.Comment.CommentBuilder;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.TreeNode;
import littleware.asset.test.AbstractAssetTest;
import littleware.base.Maybe;

/**
 * Just verify that we can save and retrieve a comment
 */
public class CommentTester extends AbstractAssetTest {

    {
        setName("testComments");
    }
    private static final Logger log = Logger.getLogger(CommentTester.class.getName());
    private final AssetSearchManager search;
    private final AssetManager assetMan;
    private final Provider<TemplateBuilder> treeBuilder;
    private final AssetPathFactory pathFactory;
    private final Provider<CommentBuilder> commentProvider;

    @Inject
    public CommentTester(AssetSearchManager search, AssetManager assetMan,
            Provider<AssetTreeTemplate.TemplateBuilder> treeBuilder,
            AssetPathFactory pathFactory,
            Provider<Comment.CommentBuilder> commentProvider) {
        this.search = search;
        this.assetMan = assetMan;
        this.treeBuilder = treeBuilder;
        this.pathFactory = pathFactory;
        this.commentProvider = commentProvider;
    }
    Maybe<TreeNode> testFolder = Maybe.empty();

    @Override
    public void setUp() {
        try {
            for (AssetTreeTemplate.AssetInfo info :
                    treeBuilder.get().assetBuilder("CommentTester").build().visit(getTestHome(search), search)) {
                if (!info.getAssetExists()) {
                    testFolder = Maybe.something(
                            assetMan.saveAsset(info.getAsset(), "setting up test folder")
                            );
                } else {
                    testFolder = Maybe.something(info.getAsset());
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Setup failed", ex);
            fail("Setup caught exception: " + ex);
        }
    }

    @Override
    public void tearDown() {
        try {
            for (Asset folder : testFolder) {
                for (UUID id : search.getAssetIdsFrom(folder.getId(), Comment.COMMENT_TYPE).values()) {
                    assetMan.deleteAsset(id, "tearDown test");
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed comment cleanup", ex);
            fail("tearDown caught: " + ex);
        }
    }

    public void testComments() {
        try {
            final TreeNode folder = testFolder.get();
            final String commentString = "Test bla bla bla!";
            final Comment comment = assetMan.saveAsset(
                    commentProvider.get().parent( folder
                    ).name("testComment" + (new Date()).getTime()
                    ).fullText(commentString).build(),
                    "Save test comment"
                    );
            assertTrue( "Post save preserves comment: " + comment.getFullText(),
                    comment.getFullText().equals( commentString ));
            final Comment load = search.getAsset( comment.getId() ).get().narrow();
            assertTrue( "Post load preserves comment: " + load.getFullText(),
                    load.getFullText().equals( commentString )
                    );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        }
    }
}
