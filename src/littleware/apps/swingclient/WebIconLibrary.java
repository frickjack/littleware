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
 * under /littleware/lib/icons.
 */
public class WebIconLibrary implements IconLibrary {
    private Map<AssetType,Icon>  ov_asset_icons = new HashMap<AssetType,Icon> ();
    private Map<String,Icon>     ov_named_icons = new HashMap<String,Icon> ();
    private String               os_server = "littleware.frickjack.com";
    
    /**
     * Configure library to pull from default server: littleware.frickjack.com
     */
    public WebIconLibrary () {
        try {
            setIconServer ( os_server );
        } catch ( MalformedURLException e ) {
            throw new AssertionFailedException ( "Should not have had problems with default icon server, but caught: " + e, e );
        }
    }
    
    /**
     * Configure library to pull icons from the specified server
     *
     * @param s_server hostname with the expected icon directory structure
     * @exception MalformedURLException if s_server leads to illegal URL
     */
    public WebIconLibrary ( String s_server ) throws MalformedURLException {
        setIconServer ( s_server );
    }
    
    
    /**
     * Configure the littleware server from which to load the UI .gif icons:
     *            http://s_server/littleware/lib/icons/apache/a.gif,
     *            http://s_server/littleware/lib/icons/apache/right.gif
     * This is a stupid temporary hook to support applets for now.
     * Should introduce a more configurable IconManager later.
     *
     * @param s_server to access
     * @exception MalformedURLException if s_server leads to illegal URL
     */
    public void setIconServer ( String s_server ) throws MalformedURLException {
        ov_named_icons.put ( "littleware.bomb",
                             new ImageIcon ( new URL ( "http://" + s_server +
                                               "/littleware/lib/icons/apache/bomb.gif" ) 
                                     )
                             );
        ov_named_icons.put ( "littleware.screw",
                             new ImageIcon ( new URL ( "http://" + s_server +
                                                "/littleware/lib/icons/apache/screw2.gif" ) 
                                      )
                             );
        ov_named_icons.put ( "littleware.right_arrow",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/arrow_closed_active_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.down_arrow",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/arrow_closing_active_16.gif" ) 
                                            )
                             );        
        ov_named_icons.put ( "littleware.calendar",
                             new ImageIcon ( new URL ( "http://" + s_server +
                                                   "/littleware/lib/icons/geronimo/cal_16.gif" ) 
                                         )
                             );   
        
        ov_named_icons.put ( "littleware.back",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/back_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.forward",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/forwd_16.gif" ) 
                                            )
                             );
        
        ov_named_icons.put ( "littleware.edit",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/edit_blogentry_16.gif" ) 
                                            )
                             );
        
        ov_named_icons.put ( "littleware.addnew",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/add_page_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.apply",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/ref_16.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.browse",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/srch_16.gif" ) 
                                            )
                             );

        ov_named_icons.put ( "littleware.goto",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/linkext7.gif" ) 
                                            )
                             );
        ov_named_icons.put ( "littleware.delete",
                             new ImageIcon( new URL ( "http://" +
                                                      s_server + 
                                                      "/littleware/lib/icons/geronimo/del_16.gif" ) 
                                            )
                             );
        
        //....................
        ov_asset_icons.put ( AssetType.GENERIC,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                                       "/littleware/lib/icons/geronimo/foldr_16.gif" ) 
                                             )
                             );
        
        ov_asset_icons.put ( SecurityAssetType.USER,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                               "/littleware/lib/icons/geronimo/user_16.gif" ) 
                                     )
                             );
        ov_asset_icons.put ( SecurityAssetType.GROUP,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                                "/littleware/lib/icons/geronimo/group_16.gif" ) 
                                      )
                             );
        ov_asset_icons.put ( SecurityAssetType.ACL,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                              "/littleware/lib/icons/geronimo/lock_16.gif" ) 
                                    )
                             );
        ov_asset_icons.put ( AssetType.HOME,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                               "/littleware/lib/icons/geronimo/home_16.gif" ) 
                                     )
                             );
        ov_asset_icons.put ( AddressAssetType.CONTACT,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                                       "/littleware/lib/icons/geronimo/addbk_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( AddressAssetType.ADDRESS,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                                       "/littleware/lib/icons/geronimo/addbk_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( TrackerAssetType.TASK,
                             new ImageIcon ( new URL ( "http://" + s_server + 
                                                       "/littleware/lib/icons/geronimo/go_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( TrackerAssetType.COMMENT,
                             new ImageIcon ( new URL ( "http://" + s_server + 
                                                       "/littleware/lib/icons/geronimo/list_pages_16.gif" ) 
                                             )
                             );
        ov_asset_icons.put ( TrackerAssetType.QUEUE,
                             new ImageIcon ( new URL ( "http://" + s_server + 
                                                       "/littleware/lib/icons/geronimo/trafficlight_green_16.png" ) 
                                             )
                             );
        ov_asset_icons.put ( AssetType.LINK,
                             new ImageIcon ( new URL ( "http://" + s_server +
                                                       "/littleware/lib/icons/geronimo/link_16.gif" ) 
                                             )
                             );
        
    }
    
    /** Get the host this is pulling icons from */
    public String getIconServer () { return os_server; }

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
