/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingbase;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import java.net.URL;
import java.util.Properties;
import littleware.apps.swingclient.FeedbackBundle;
import littleware.base.feedback.Feedback;

/**
 * Setup basic bindings for Feedback and whatever for swing.base framework.
 */
public class SwingBaseGuice implements Module {
    private final String appName;
    private final String version;
    private final URL helpUrl;
    private final Properties defaultProperties;
    /**
     * Inject swingbase.BaseData properties - binds BaseData.* @Named
     * constants at configure time.
     */
    public SwingBaseGuice( String appName, String version,
            URL helpUrl, Properties defaultProperties
            ) {
        this.appName = appName;
        this.version = version;
        this.helpUrl = helpUrl;
        this.defaultProperties = defaultProperties;
    }

    
    @Provides @Singleton
    public Feedback provideFeedback( FeedbackBundle fbBundle ) {
        return fbBundle.getFeedback();
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(FeedbackBundle.class).in( Scopes.SINGLETON );
        binder.bindConstant().annotatedWith(Names.named("BaseData.appName" ) ).to( appName );
        binder.bindConstant().annotatedWith(Names.named("BaseData.version" ) ).to( version );
        binder.bind( URL.class ).annotatedWith(Names.named("BaseData.helpUrl" ) ).toInstance(helpUrl);
        binder.bind( Properties.class ).annotatedWith(Names.named("BaseData.defaultProperties" ) ).toInstance( defaultProperties );
    }

}
