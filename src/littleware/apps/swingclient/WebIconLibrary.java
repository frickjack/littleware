package littleware.apps.swingclient;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.net.URL;
import java.net.MalformedURLException;
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
public class WebIconLibrary implements IconLibrary {
    private Map<AssetType,Icon>  ov_asset_icons = new HashMap<AssetType,Icon> ();
    private Map<String,Icon>     ov_named_icons = new HashMap<String,Icon> ();
    private String               os_url_root = "littleware.frickjack.com/littleware/lib/icons";
    
    /**
     * Configure library to pull from default root url: 
     *      littleware.frickjack.com/littleware/lib/icons
     */
    public WebIconLibrary () {
        try {
            setIconServer ( os_url_root );
        } catch ( MalformedURLException e ) {
            throw new AssertionFailedException ( "Should not have had problems with default icon server, but caught: " + e, e );
        }
    }
    
    /**
     * Configure library to pull icons from the specified server
     *
     * @param s_url_root hostname/rootdir under which
     *                     the expected icon directory structure
     *                     http://s_url_root/hierarchy
     * @exception MalformedURLException if s_url_root leads to 
     *                                   illegal URL
     */
    public WebIconLibrary ( String s_url_root ) throws MalformedURLException {
        setIconServer ( s_url_root );
    }
    
    
    /**
     * Configure the littleware server from which to load the UI .gif icons:
     *            http://s_url_root/apache/a.gif,
     *            http://s_url_root/apache/right.gif
     * This is a stupid temporary hook to support applets for now.
     * Should introduce a more configurable IconManager later.
     *
     * @param s_url_root to access
     * @exception MalformedURLException if s_url_root leads to illegal URL
     */
    private void setIconServer ( String s_url_root ) throws MalformedURLException {
        ov_named_icons.put ( "littleware.bomb",
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                               "/apache/bomb.gif" ) 
                                     )
                             );
        ov_named_icons.put ( "littleware.screw",
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                                "/apache/screw2.gif" ) 
                                      )
                             );
        ov_named_icons.put ( "littleware.right_arrow",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/arrow_closed_active_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.down_arrow",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/arrow_closing_active_16.gif" ) 
                                            )
                             );        
        ov_named_icons.put ( "littleware.calendar",
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                                   "/geronimo/cal_16.gif" ) 
                                         )
                             );   
        
        ov_named_icons.put ( "littleware.back",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/back_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.forward",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/forwd_16.gif" ) 
                                            )
                             );
        
        ov_named_icons.put ( "littleware.edit",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/edit_blogentry_16.gif" ) 
                                            )
                             );
        
        ov_named_icons.put ( "littleware.addnew",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/add_page_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.apply",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/ref_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.browse",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/srch_16.gif" ) 
                                            )
                             );

        ov_named_icons.put ( "littleware.goto",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/linkext7.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.delete",
                             new ImageIcon( new URL ( "http://" +
                                                      s_url_root + 
                                                      "/geronimo/del_16.gif" ) 
                                            )
                             );
        
        //....................
        ov_asset_icons.put ( AssetType.GENERIC,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                                       "/geronimo/foldr_16.gif" ) 
                                             )
                             );
        
        ov_asset_icons.put ( SecurityAssetType.USER,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                               "/geronimo/user_16.gif" ) 
                                     )
                             );
        ov_asset_icons.put ( SecurityAssetType.GROUP,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                                "/geronimo/group_16.gif" ) 
                                      )
                             );
        ov_asset_icons.put ( SecurityAssetType.ACL,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                              "/geronimo/lock_16.gif" ) 
                                    )
                             );
        ov_asset_icons.put ( AssetType.HOME,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                               "/geronimo/home_16.gif" ) 
                                     )
                             );
        ov_asset_icons.put ( AddressAssetType.CONTACT,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                                       "/geronimo/addbk_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( AddressAssetType.ADDRESS,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                                       "/geronimo/addbk_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( TrackerAssetType.TASK,
                             new ImageIcon ( new URL ( "http://" + s_url_root + 
                                                       "/geronimo/go_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( TrackerAssetType.COMMENT,
                             new ImageIcon ( new URL ( "http://" + s_url_root + 
                                                       "/geronimo/list_pages_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( TrackerAssetType.QUEUE,
                             new ImageIcon ( new URL ( "http://" + s_url_root + 
                                                       "/geronimo/trafficlight_green_16.png" ) 
                                             )
                             );
        ov_asset_icons.put ( AssetType.LINK,
                             new ImageIcon ( new URL ( "http://" + s_url_root +
                                                       "/geronimo/link_16.gif" ) 
                                             )
                             );        
    }
    

    public Icon  lookupIcon ( AssetType n_asset ) {
        Icon icon_result = ov_asset_icons.get ( n_asset );
        if ( icon_result != null ) {
            return icon_result;
        }
        if ( n_asset.isA ( AssetType.LINK ) ) {
            return ov_asset_icons.get ( AssetType.LINK );
        }
        return ov_asset_icons.get ( AssetType.GENERIC );
    }
    
    public Icon  lookupIcon ( String s_icon ) {
        return ov_named_icons.get ( s_icon );
    }
    
    public Set<AssetType> getIconAssetTypes () {
        return Collections.unmodifiableSet( ov_asset_icons.keySet () );
    }
    
    public Set<String> getIconNames () {
        return Collections.unmodifiableSet ( ov_named_icons.keySet () );
    }
    
}
