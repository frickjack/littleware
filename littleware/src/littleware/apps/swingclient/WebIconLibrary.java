package littleware.apps.swingclient;

import com.google.inject.Singleton;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import littleware.base.AssertionFailedException;
import littleware.asset.AssetType;
import littleware.security.SecurityAssetType;
import littleware.apps.addressbook.AddressAssetType;
import littleware.apps.tracker.TrackerAssetType;

/**
 * Icon library that builds icons from images
 * referenced off the littleware web site
 * under s_url_root.
 * Default s_url_root is littleware.frickjack.com/littleware/lib/icons
 */
@Singleton
public class WebIconLibrary implements IconLibrary {
    private static final Logger  olog = Logger.getLogger( WebIconLibrary.class.getName() );
    private static final Map<AssetType,String>   ov_asset_urls= new HashMap<AssetType,String> ();
    private static final Map<String,String>      ov_named_urls = new HashMap<String,String> ();
    
    private Map<AssetType,Icon>  ov_asset_icons = new HashMap<AssetType,Icon> ();
    private Map<String,Icon>     ov_named_icons = new HashMap<String,Icon> ();
    
    static {                
        ov_named_urls.put ( "littleware.bomb",                              
                                               "/apache/bomb.gif"                                      
                             );
        ov_named_urls.put ( "littleware.screw",                              
                                                "/apache/screw2.gif" 
                             );
        ov_named_urls.put ( "littleware.right_arrow",
                                                      "/geronimo/arrow_closed_active_16.gif" 
                             );
        ov_named_urls.put ( "littleware.down_arrow",
                                                      "/geronimo/arrow_closing_active_16.gif" 
                             );        
        ov_named_urls.put ( "littleware.calendar",
                                                   "/geronimo/cal_16.gif"  
                             );   
        
        ov_named_urls.put ( "littleware.back",
                                                      "/geronimo/back_16.gif" 
                             );
        ov_named_urls.put ( "littleware.forward",
                                                      "/geronimo/forwd_16.gif" 
                             );
        
        ov_named_urls.put ( "littleware.edit",
                                                      "/geronimo/edit_blogentry_16.gif" 
                             );
        
        ov_named_urls.put ( "littleware.addnew",
                                                      "/geronimo/add_page_16.gif" 
                             );
        ov_named_urls.put ( "littleware.apply",
                                                      "/geronimo/ref_16.gif" 
                             );
        ov_named_urls.put ( "littleware.browse",
                                                      "/geronimo/srch_16.gif" 
                             );

        ov_named_urls.put ( "littleware.goto",
                                                      "/geronimo/linkext7.gif" 
                             );
        ov_named_urls.put ( "littleware.delete",
                                                      "/geronimo/del_16.gif" 
                             );
        
        //....................
        ov_asset_urls.put ( AssetType.GENERIC,
                                                       "/geronimo/foldr_16.gif" 
                             );
        
        ov_asset_urls.put ( SecurityAssetType.USER,
                                               "/geronimo/user_16.gif" 
                             );
        ov_asset_urls.put ( SecurityAssetType.GROUP,
                                                "/geronimo/group_16.gif" 
                             );
        ov_asset_urls.put ( SecurityAssetType.ACL,
                                              "/geronimo/lock_16.gif" 
                             );
        ov_asset_urls.put ( AssetType.HOME,
                                               "/geronimo/home_16.gif" 
                             );
        ov_asset_urls.put ( AddressAssetType.CONTACT,
                                                       "/geronimo/addbk_16.gif" 
                             );
        ov_asset_urls.put ( AddressAssetType.ADDRESS,
                                                       "/geronimo/addbk_16.gif" 
                             );
        ov_asset_urls.put ( TrackerAssetType.TASK,
                                                       "/geronimo/go_16.gif" 
                             );
        ov_asset_urls.put ( TrackerAssetType.COMMENT,
                                                       "/geronimo/list_pages_16.gif" 
                             );
        ov_asset_urls.put ( TrackerAssetType.QUEUE,
                                                       "/geronimo/trafficlight_green_16.png" 
                             );
        ov_asset_urls.put ( AssetType.LINK,
                                                       "/geronimo/link_16.gif" 
                             );        

    }
    
    private String               os_url_root = "littleware.frickjack.com/littleware/lib/icons";
    
    /**
     * Configure library to pull from default root url: 
     *      littleware.frickjack.com/littleware/lib/icons
     */
    public WebIconLibrary () {
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
     * @exception MalformedURLException if s_url_root leads to illegal URL
     */
    public void setRoot ( String s_url_root ) throws MalformedURLException {
        os_url_root = s_url_root;        
    }
    
    public String getRoot() { return os_url_root; }

    public Icon  lookupIcon ( AssetType n_asset ) {
        Icon icon_result = ov_asset_icons.get ( n_asset );
        if ( icon_result != null ) {
            return icon_result;
        }
        String s_url_tail = ov_asset_urls.get( n_asset );
        if ( null != s_url_tail ) {
            try {
                URL url_icon = new URL ( "http://" + os_url_root + s_url_tail );
                icon_result = new ImageIcon( url_icon );
            } catch ( MalformedURLException e ) {
                olog.log( Level.WARNING, "Invalid icon root property, caught: " + e );
                icon_result = new ImageIcon ();
            }
            ov_asset_icons.put( n_asset, icon_result );
            return icon_result;
        }
        if ( n_asset.isA ( AssetType.LINK ) ) {
            return lookupIcon( AssetType.LINK );
        }
        if ( n_asset.equals( AssetType.GENERIC ) ) {
            throw new AssertionFailedException( "No icon registered for Generic asset type?" );
        }
        return lookupIcon( AssetType.GENERIC );
    }
    
    public Icon  lookupIcon ( String s_icon ) {
        Icon icon_result = ov_named_icons.get ( s_icon );
        if ( null != icon_result ) {
            return icon_result;
        }
        String s_url_tail = ov_named_urls.get( s_icon );
        if ( null == s_url_tail ) {
            return null;
        }
        try {
            URL url_icon = new URL ( "http://" + os_url_root + s_url_tail );
            icon_result = new ImageIcon( url_icon );
        } catch( MalformedURLException e ) {
            olog.log( Level.WARNING, "Failed to map icon URL, caught: " + e );
            icon_result = new ImageIcon ();
        }
        ov_named_icons.put( s_icon, icon_result );
        return icon_result;
    }
    
    public Set<AssetType> getIconAssetTypes () {
        return Collections.unmodifiableSet( ov_asset_urls.keySet () );
    }
    
    public Set<String> getIconNames () {
        return Collections.unmodifiableSet ( ov_named_urls.keySet () );
    }
    
}
