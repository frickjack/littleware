/*
 * Copyright (c) 2007,2008 Reuben Pasquini
 * All Rights Reserved
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import com.google.inject.Binder;
import com.google.inject.Module;

import com.google.inject.Scopes;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import littleware.apps.client.*;

/**
 * Specialize ClientServiceGuice to include bindings
 * for the littleware.apps.swingclient package.
 * 
 * @author pasquini
 */
public class StandardSwingGuice implements Module {
    
    
    /** 
     * Client must inject SessionHelper dependency 
     * by hand via setSessionHelper
     */
    public StandardSwingGuice () {}
    
    
    @Override
    public void configure( Binder binder ) {
        binder.bind( AssetViewFactory.class ).to( SimpleAssetViewFactory.class );
        binder.bind( AssetEditorFactory.class ).to( EditorAssetViewFactory.class );
        binder.bind( IconLibrary.class ).to( WebIconLibrary.class ).in( Scopes.SINGLETON );
        binder.bind( ListModel.class ).to( DefaultListModel.class );
    }

}
