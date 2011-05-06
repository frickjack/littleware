/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.UUID;
import java.util.Date;
import littleware.base.cache.CacheableObject;

/**
 * Asset data-bucket base-class.
 * A typical littleware application arranges
 * assets into a tree-like graph rooted under
 * the application's "home" asset.
 * The application arranges assets into different
 * subtrees to categorize the assets in different ways.
 * For example, a request-tracker application might
 * have a very simple structure.
 *     /Application/InBox/
 *     /Application/OutBox/
 * where /Application is a 'home' type asset (see littleware.asset.AssetType),
 * InBox and OutBox are 'generic' assets, and InBox and OutBox
 * have multiple 'request' type asset children.
 */
public interface Asset extends CacheableObject {

    public String getName();

    /** Id of user that created this asset */
    public UUID getCreatorId();

    /** Id of user that last updated this asset */
    public UUID getLastUpdaterId();

    public UUID getAclId();

    public AssetType getAssetType();

    /** Each asset has a comment attached to it */
    public String getComment();

    /**
     * Get comment/log-message associated with the last update
     * to this asset.
     */
    public String getLastUpdate();

    /**
     * Id of home-asset this asset is associated with - should never be null
     */
    public UUID getHomeId();

    /**
     * Every asset (except home-type LittleHome) assets link from
     * some other asset in the node graph.
     * An asset subtype may alias the fromId property depending on
     * its application - ex: TreeNode's parentId property is an alias for fromId.
     * Note that the AssetBuilder does not include the fromId property -
     * a subtype determine how the fromId gets set.
     */
    public UUID getFromId();

    /**
     * Id of Principal that owns this asset - may be null.
     * Members of
     * the littewlare.admin group are also implicit owners
     * of every object.
     */
    public UUID getOwnerId();


    public Date getCreateDate();

    public Date getLastUpdateDate();


    /**
     * Shortcut for a.getAssetType().create().copy( a )
     */
    public AssetBuilder copy();

    /** Cast this to the specified asset type ... little safer than simple cast */
    public <T extends Asset> T narrow(Class<T> type);

    public <T extends Asset> T narrow();

}
    
