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
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import littleware.apps.swingbase.model.BaseData;

/**
 * Save the properties associated with BaseData
 */
public class SaveOptionsAction extends AbstractAction {
    private static final Logger log = Logger.getLogger( SaveOptionsAction.class.getName() );

    private final BaseData data;
    private final SwingBaseTool tool;

    @Inject
    public SaveOptionsAction( BaseData data, SwingBaseTool tool ) {
        super( "Save" );
        this.data = data;
        this.tool = tool;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            tool.saveProps(data);
        } catch ( Exception ex ) {
            JOptionPane.showMessageDialog(null, "Save failed: " + ex, "Error",
                    JOptionPane.ERROR_MESSAGE
                    );
        }
    }
}
