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
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import littleware.apps.swingclient.FeedbackBundle;
import littleware.base.feedback.Feedback;

/**
 * Setup basic bindings for Feedback and whatever for swing.base framework.
 */
public class SwingBaseGuice implements Module {

    @Provides @Singleton
    public Feedback provideFeedback( FeedbackBundle fbBundle ) {
        return fbBundle.getFeedback();
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(FeedbackBundle.class).in( Scopes.SINGLETON );
    }

}
