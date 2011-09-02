/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import com.google.inject.ImplementedBy;
import java.util.Set;
import javax.swing.Icon;

import littleware.asset.Asset;
import littleware.asset.AssetType;


/**
 * Interface for retrieving different kinds of Icons.
 */
@ImplementedBy(WebIconLibrary.class)
public interface IconLibrary {
    /**
     * Utility interface - allows us to register an IconProvider
     * that return an icon based on icon-state.
     */
    public interface IconProvider {
        public Icon getIcon( Asset aNeedsIcon );
        public Icon getIcon( AssetType atypeNeedsIcon );
    }

    /**
     * Configure the root path from which to load the UI .gif icons.
     * A web-based icon library might expand the root out like this:
     *            http://s_url_root/apache/a.gif,
     *            http://s_url_root/apache/right.gif
     *
     * @param s_root hostname/rootdir under which
     *                     the expected icon directory structure
     *                     http://s_url_root/hierarchy
     * @throws MalformedURLException if s_url_root leads to illegal URL
     */
    public void setRoot ( String s_url_root ) throws java.net.MalformedURLException;
    
    public String getRoot();

    /**
     * Get a reference to the most specific mini-icon available for the given
     * asset type - return the generic asset icon if nothing specific available.
     *
     * @param n_asset type of asset we want an icon for
     * @return the icon 
     */
    public Icon  lookupIcon ( AssetType n_asset );

    /**
     * Get a reference to the most specific mini-icon available for the given asset's
     * asset type - return the generic asset icon if nothing specific available.
     * Allows selection of icon based on asset-state in addition to asset-type.
     * In simple case might just call through to lookupIcon( asset.getAssetType() ).
     *
     * @param asset to lookup
     * @return the icon
     */
    public Icon  lookupIcon ( Asset asset );

    /**
     * Register a new icon for the given asset type
     * 
     * @param n_asset
     * @param icon
     * @return the previously registered icon or null
     */
    public void  registerIcon( AssetType n_asset, IconProvider provideIcon );
    /**
     * Equivalent to registering and IconProvider that always returns
     * the same icon.
     */
    public void  registerIcon( AssetType n_asset, Icon  icon );
    
    /**
     * Get the set of asset-types that have an icon or
     * icon-provider registered
     */
    public Set<AssetType> getIconAssetTypes ();
    
    /**
     * Get a reference to an icon associated with a given name.
     *
     * @param s_name to lookup
     * @return the icon or null if none registered with that name
     */
    public Icon  lookupIcon ( String s_name );
    /**
     * Register an icon with the given name
     *
     * @return the previously registered icon or null
     */
    public void registerIcon( String s_name, Icon icon );
    
    /**
     * Get the set of names in the library
     */
    public Set<String> getIconNames ();
}
