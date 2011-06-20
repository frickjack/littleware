/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset;

import java.util.UUID;
import littleware.base.UUIDFactory;

/**
 * Marker for home-type assets
 */
public interface LittleHome extends TreeParent {
    /** 
     * Name of the littleware.home asset
     */
    public static final String LITTLE_HOME = "littleware.home";
    public static final UUID   LITTLE_HOME_ID = UUIDFactory.parseUUID( "BD46E5588F9D4F41A6310100FE68DCB4" );
    
    /** HOME asset-type - must be admin to create */
    public static final AssetType HOME_TYPE = new AssetType(UUIDFactory.parseUUID("C06CC38C6BD24D48AB5E2D228612C179"),
            "littleware.HOME") {

        /** Always return true */
        @Override
        public boolean isAdminToCreate() {
            return true;
        }

        /** Always return true */
        @Override
        public boolean isNameUnique() {
            return true;
        }
    };


    public interface HomeBuilder extends AssetBuilder {
        @Override
        public LittleHome build();
    }
}
