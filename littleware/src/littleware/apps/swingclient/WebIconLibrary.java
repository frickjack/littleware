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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
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

import littleware.asset.Asset;
import littleware.base.AssertionFailedException;
import littleware.asset.AssetType;
import littleware.security.SecurityAssetType;
import littleware.apps.addressbook.AddressAssetType;
import littleware.apps.tracker.TrackerAssetType;

/**
 * Icon library that builds icons from images
 * referenced off the littleware web site or
 * from a jar in the classpath
 * under s_url_root.
 * Default s_url_root is http://littleware.frickjack.com/littleware/lib/icons,
 * but little_icons.jar has icons under littleware/apps/swingclient/icons.
 */
@Singleton
public class WebIconLibrary implements IconLibrary {
    private static final Logger  olog = Logger.getLogger( WebIconLibrary.class.getName() );
    private static final Map<AssetType,String>   ov_asset_urls= new HashMap<AssetType,String> ();
    private static final Map<String,String>      ov_named_urls = new HashMap<String,String> ();
    
    private final Map<AssetType,IconLibrary.IconProvider>  ov_asset_icons =
            new HashMap<AssetType,IconLibrary.IconProvider> ();
    private final Map<String,Icon>     ov_named_icons =
            new HashMap<String,Icon> ();
    
    static {                
        ov_named_urls.put ( "littleware.bomb",                              
                                               "/apache/bomb.gif"                                      
                             );
        ov_named_urls.put ( "littleware.screw",                              
                                                "/apache/screw2.gif" 
                             );
        ov_named_urls.put ( "littleware.wait",
                            "/geronimo/exp_32.gif"
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
        ov_named_urls.put ( "littleware.up",
                                                      "/geronimo/up_16.gif"
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
        ov_named_urls.put ( "littleware.refresh",
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
        ov_asset_urls.put ( SecurityAssetType.GROUP_MEMBER,
                                            "/geronimo/link_16.gif"
                             );

        ov_asset_urls.put ( SecurityAssetType.ACL,
                                              "/geronimo/lock_16.gif" 
                             );
        ov_asset_urls.put ( SecurityAssetType.ACL_ENTRY,
                                            "/geronimo/link_16.gif"
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
    
    private String               os_url_root = "http://littleware.frickjack.com/littleware/lib/icons";
    
    /**
     * Inject root-url for icons
     */
    @Inject
    public WebIconLibrary( @Named( "icon.base_url" ) String s_url_root ) {
        olog.log( Level.FINE, "Setting icon base to: " + s_url_root );
        os_url_root = s_url_root;
    }
    
    /**
     * Configure library to pull from default root url: 
     *      littleware.frickjack.com/littleware/lib/icons
     *
    public WebIconLibrary () {
    }
     */
    
    
    
    /**
     * Configure the root path from which to load the UI .gif icons.
     * A web-based icon library might expand the root out like this:
     *            s_url_root/apache/a.gif,
     *            s_url_root/apache/right.gif
     * , if the url does not start with http:, then assumed to 
     * reference the classpath.
     *
     * @param s_root hostname/rootdir under which
     *                     the expected icon directory structure
     *                     s_url_root/hierarchy
     * @exception MalformedURLException if s_url_root leads to illegal URL
     */
    @Override
    public void setRoot ( String s_url_root ) throws MalformedURLException {
        os_url_root = s_url_root;        
    }
    
    @Override
    public String getRoot() { return os_url_root; }

    private static class DefaultProvider implements IconLibrary.IconProvider {
        private final Icon oicon;
        public DefaultProvider( Icon icon ) {
            oicon = icon;
        }

        @Override
        public Icon getIcon(Asset aNeedsIcon) {
            return oicon;
        }

        @Override
        public Icon getIcon(AssetType atypeNeedsIcon) {
            return oicon;
        }

    }

    /**
     * Internal method maintains the icon-provider cache
     */
    private IconLibrary.IconProvider lookupProvider( AssetType atype ) {
        IconLibrary.IconProvider provider = ov_asset_icons.get ( atype );
        if ( provider != null ) {
            return provider;
        }
        String s_url_tail = ov_asset_urls.get( atype );
        if ( null != s_url_tail ) {
            try {
                URL url_icon = null;
                String s_full_url = os_url_root + s_url_tail;
                if ( os_url_root.startsWith( "http:" ) ) {
                    url_icon = new URL ( s_full_url );
                } else {
                    url_icon = Thread.currentThread().getContextClassLoader().getResource( s_full_url );
                }
                if ( null == url_icon ) { // try going to the System ClassLoader
                    url_icon = ClassLoader.getSystemResource( s_full_url );
                }

                if ( null != url_icon ) {
                    provider = new DefaultProvider( new ImageIcon( url_icon ) );
                } else {
                    olog.log( Level.WARNING, "Unable to find asset type icon resource: " + s_full_url );
                    provider = new DefaultProvider( new ImageIcon() );
                }
            } catch ( MalformedURLException e ) {
                olog.log( Level.WARNING, "Invalid icon root property, caught: " + e );
                provider = new DefaultProvider( new ImageIcon () );
            }
            ov_asset_icons.put( atype, provider );
            return provider;
        }
        if ( atype.isA ( AssetType.LINK ) ) {
            return lookupProvider( AssetType.LINK );
        }
        if ( atype.equals( AssetType.GENERIC ) ) {
            throw new AssertionFailedException( "No icon registered for Generic asset type?" );
        }
        return lookupProvider( AssetType.GENERIC );
    }

    @Override
    public Icon  lookupIcon ( AssetType n_asset ) {
        return lookupProvider( n_asset ).getIcon( n_asset );
    }

    @Override
    public void registerIcon( AssetType n_asset, Icon icon ) {
        ov_asset_icons.put( n_asset, new DefaultProvider( icon ) );
    }

    @Override
    public Icon  lookupIcon ( String s_icon ) {
        Icon icon_result = ov_named_icons.get ( s_icon );
        if ( null != icon_result ) {
            return icon_result;
        }
        String s_url_tail = ov_named_urls.get( s_icon );
        if ( null == s_url_tail ) {
            olog.log( Level.WARNING, "Request for unregistered icon: " + s_icon );
            return null;
        }
        try {   
            String s_full_url = os_url_root + s_url_tail;
            URL url_icon = null;
            if ( os_url_root.startsWith( "http:" ) ) {
                url_icon = new URL ( s_full_url );
            } else {
                url_icon = Thread.currentThread().getContextClassLoader().getResource( s_full_url );
            }
            if ( null == url_icon ) { // try going to the System ClassLoader
                url_icon = ClassLoader.getSystemResource( s_full_url );
            }
            
            olog.log( Level.FINE, "Loading icon: " + url_icon );
            if ( null != url_icon ) {
                icon_result = new ImageIcon( url_icon );
            } else {
                olog.log( Level.WARNING, "Unable to find icon resource: " + s_full_url );
                icon_result =new ImageIcon ();
            }
        } catch( Exception e ) {
            olog.log( Level.WARNING, "Failed to map icon URL, caught: " + e );
            icon_result = new ImageIcon ();
        }
        ov_named_icons.put( s_icon, icon_result );
        return icon_result;
    }

    @Override
    public void registerIcon( String s_name, Icon icon ) {
        ov_named_icons.put( s_name, icon);
    }
    
    @Override
    public Set<AssetType> getIconAssetTypes () {
        return Collections.unmodifiableSet( ov_asset_urls.keySet () );
    }
    
    @Override
    public Set<String> getIconNames () {
        return Collections.unmodifiableSet ( ov_named_urls.keySet () );
    }

    @Override
    public Icon lookupIcon(Asset asset) {
        return lookupProvider( asset.getAssetType() ).getIcon( asset );
    }

    @Override
    public void registerIcon(AssetType n_asset, IconLibrary.IconProvider provideIcon) {
        ov_asset_icons.put( n_asset, provideIcon );
    }
    
}
