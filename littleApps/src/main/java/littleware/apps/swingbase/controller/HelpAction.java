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
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import littleware.apps.swingbase.model.BaseData;

/**
 * Action to shutdown application - just invokes injected ShutdownHandler
 */
public class HelpAction extends AbstractAction {
    private static final Logger log = Logger.getLogger( HelpAction.class.getName() );
    private final BaseData data;

    @Inject
    public HelpAction( BaseData data ) {
        super( "Help" );
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(data.getHelpURL().toString()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to launch browser for " +
                    data.getHelpURL() + " (" + ex + ")"
                    );
            log.log( Level.WARNING, "Failed to open help URI", ex );
        }
    }

}
