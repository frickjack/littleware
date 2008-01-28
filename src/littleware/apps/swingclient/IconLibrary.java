package littleware.apps.swingclient;

import com.google.inject.ImplementedBy;
import java.util.Set;
import javax.swing.Icon;

import littleware.asset.AssetType;


/**
 * Interface for retrieving different kinds of Icons.
 */
@ImplementedBy(WebIconLibrary.class)
public interface IconLibrary {

        /**
     * Configure the root path from which to load the UI .gif icons.
     * A web-based icon library might expand the root out like this:
     *            http://s_url_root/apache/a.gif,
     *            http://s_url_root/apache/right.gif
     *
     * @param s_root hostname/rootdir under which
     *                     the expected icon directory structure
     *                     http://s_url_root/hierarchy
     * @exception MalformedURLException if s_url_root leads to illegal URL
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
     * Get the set of asset-types that have an icon registered
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
     * Get the set of names in the library
     */
    public Set<String> getIconNames ();
}
