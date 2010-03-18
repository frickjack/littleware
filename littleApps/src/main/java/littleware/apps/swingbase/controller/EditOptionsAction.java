/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.swingbase.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.view.JPropEditor;

/**
 * Display the BaseData properties editor
 */
@Singleton
public class EditOptionsAction extends AbstractAction {
    private final BaseData data;
    private final JPropEditor  jeditor;

    @Inject
    public EditOptionsAction( BaseData data, JPropEditor jeditor,
            SaveOptionsAction saveAction
            ) {
        this.data = data;
        this.jeditor = jeditor;
    }

    @Override
    public void actionPerformed(ActionEvent event ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
