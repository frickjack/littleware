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
import javax.swing.AbstractAction;

/**
 * Action to shutdown application - just invokes injected ShutdownHandler
 */
public class ExitAction extends AbstractAction {
    private final ShutdownHandler shutdown;

    @Inject
    public ExitAction( ShutdownHandler shutdown ) {
        super( "Exit" );
        this.shutdown = shutdown;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        shutdown.requestShutdown();
    }

}
