/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingbase.model;

import com.google.inject.ImplementedBy;
import com.google.inject.Singleton;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;
import littleware.base.event.LittleBean;

/**
 * Application session data
 */
public interface BaseData extends LittleBean {
    /**
     * Application name - key for session-properties storage
     */
    public String   getAppName();
    public String   getVersion();
    public URL      getHelpUrl();
    public Map<String,String>   getProperties();
    /**
     * May only update already existing property, otherwise
     * IllegalArgumentException
     */
    public void     putProperty( String name, String value );

    @ImplementedBy(SimpleBaseData.SBDBuilder.class)
    public interface BDBuilder {
        public BDBuilder appName( String value );
        public BDBuilder version( String value );
        public BDBuilder helpUrl( URL value );
        /**
         * Just invokes key.toString and value.toString
         */
        public BDBuilder putAllProps( Map<? extends Object,? extends Object> value );
        public BDBuilder putProp( String key, String value );
        public BaseData build();
    }
}
