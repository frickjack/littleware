/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingbase.test;

import com.google.inject.Inject;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.view.BaseView;
import littleware.apps.swingbase.view.BaseView.ViewBuilder;
import littleware.test.LittleTest;

/**
 * Just popup a simple do-nothing swing.base app
 */
public class SwingBaseTester extends LittleTest {
    private final BaseData model;
    private final ViewBuilder viewBuilder;

    @Inject
    public SwingBaseTester( BaseData model, BaseView.ViewBuilder viewBuilder ) {
        this.model = model;
        this.viewBuilder = viewBuilder;
    }

    public void testSwingBase() {
        final EventBarrier barrier;
    }
}
