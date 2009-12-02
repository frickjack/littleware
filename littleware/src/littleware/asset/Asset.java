/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.UUID;
import java.util.Date;
import littleware.base.CacheableObject;

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

    public String getData();

    /**
     * Source of directed link this asset represents - may be null
     */
    public UUID getFromId();

    /**
     * Destination of directed link - may be null
     */
    public UUID getToId();

    /**
     * Id of home-asset this asset is associated with - should never be null
     */
    public UUID getHomeId();

    /**
     * Id of Principal that owns this asset - may be null.
     * Members of
     * the littewlare.admin group are also implicit owners
     * of every object.
     */
    public UUID getOwnerId();

    /**
     * Asset may have a date-range associated with it
     */
    public Date getStartDate();

    /**
     * Asset may have a date-range associated with it
     */
    public Date getEndDate();

    public Date getCreateDate();

    public Date getLastUpdateDate();

    /**
     * Asset may have a float value associated with it
     * interpreted differently for different asset types
     * (priority, cost, whatever).
     *
     * @return value as an Object - so we can Proxy this interface easily
     */
    public Float getValue();

    /**
     * It's very common for asset pipelines to want to put
     * assets into one of several states.
     * Subtypes should generally map a state to an enumeration.
     *
     * @return integer asset state
     */
    public Integer getState();

    /**
     * Shortcut for a.getAssetType().create().copy( a )
     */
    public AssetBuilder copy();

    /** Cast this to the specified asset type ... little safer than simple cast */
    public <T extends Asset> T narrow(Class<T> type);

    public <T extends Asset> T narrow();

}
    
