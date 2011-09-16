/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;

/**
 * Extends base Asset with various useful properties.
 */
public interface GenericAsset extends TreeNode {
    public UUID getToId();
    
    public String getData();

    /**
     * Asset may have a date-range associated with it
     */
    public Date getStartDate();

    /**
     * Asset may have a date-range associated with it
     */
    public Date getEndDate();

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
     * Return human-readable state based on integer state and locale
     */
    public String getStateString();

    /**
     * Index of supplemental links extending from this node
     */
    public Map<String, UUID> getLinkMap();

    /**
     * Shortcut for Maybe.emtpyIfNull( getLinks().get( name ) )
     */
    public Option<UUID> getLink(String key);

    /**
     * Index of supplemental date info associated with this node
     */
    public Map<String, Date> getDateMap();

    /**
     * Shortcut for Maybe.emtpyIfNull( getDates().get( name ) )
     */
    public Option<Date> getDate(String key);

    /**
     * Get user-supplied attributes attached to this node
     */
    public Map<String, String> getAttributeMap();

    /**
     * Shortcut for Maybe.emptyIfNull( getUserAttributes().get( name ) )
     */
    public Option<String> getAttribute(String key);


    public interface GenericBuilder extends TreeNode.TreeNodeBuilder {
        public UUID getToId();

        public void setToId(UUID value);

        public GenericBuilder toId(UUID value);

        /**
         * Set the data blob attached to this asset.
         *
         * @throws IllegalArgumentException if data is in invalid format
         *                for asset type, or if length exceeds 1024 characters
         */
        public void setData(String value);

        public String getData();

        public GenericBuilder data(String value);

        public Date getStartDate();

        public void setStartDate(Date value);

        public GenericBuilder startDate(Date value);

        public Date getEndDate();

        public void setEndDate(Date value);

        public GenericBuilder endDate(Date value);

        @Override
        public GenericBuilder parentId(UUID value);

        @Override
        public GenericBuilder createDate(Date value);

        @Override
        public GenericBuilder lastUpdateDate(Date value);

        public Float getValue();

        public void setValue(float value);

        public GenericBuilder value(float value);

        public Integer getState();

        public void setState(int value);

        public GenericBuilder state(int value);

        /** name must be less than 20 characters */
        public GenericBuilder putLink(String name, UUID value);

        public Map<String, UUID> getLinkMap();

        public GenericBuilder removeLink(String name);

        /** name must be less than 20 characters */
        public GenericBuilder putDate(String name, Date value);

        public Map<String, Date> getDateMap();

        public GenericBuilder removeDate(String name);

        /** name must be less than 20 characters, value less than 128 */
        public GenericBuilder putAttribute(String name, String value);

        public Map<String, String> getAttributeMap();

        public GenericBuilder removeAttribute(String name);

        @Override
        public GenericAsset build();

        @Override
        public GenericBuilder id(UUID value);

        @Override
        public GenericBuilder name(String value);

        @Override
        public GenericBuilder creatorId(UUID value);

        @Override
        public GenericBuilder lastUpdaterId(UUID value);

        @Override
        public GenericBuilder aclId(UUID value);

        @Override
        public GenericBuilder ownerId(UUID value);

        @Override
        public GenericBuilder lastUpdate(String value);

        @Override
        public GenericBuilder homeId(UUID value);

        @Override
        public GenericBuilder timestamp(long value);

        @Override
        public GenericBuilder parent(TreeParent parent);

        @Override
        public GenericBuilder comment( String value );

    }
    /** GENERIC asset-type */
    public static final AssetType GENERIC = new AssetType(UUIDFactory.parseUUID("E18D1B19D9714F6F8F49CF9B431EBF23"),
            "littleware.GENERIC", TreeNode.TREE_NODE_TYPE ) {
    };

}
