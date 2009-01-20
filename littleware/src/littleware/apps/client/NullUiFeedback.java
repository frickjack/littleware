/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.client;

import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import littleware.apps.client.event.UiMessageEvent;
import littleware.apps.client.event.UiPublishEvent;

/**
 * UiFeedback just fires PropertyChangeEvents and LittleEvents
 * on method calls - doesn't do anything else.
 */
public class NullUiFeedback implements UiFeedback {

    private SimpleLittleTool  osupport = new SimpleLittleTool( this );

    private int oi_progress = 0;

    public int getProgress() {
        return oi_progress;
    }

    public void setProgress(int i_progress) {
        int i_clean = i_progress;
        if ( i_clean < 0 ) {
            i_clean = 0;
        } else if ( i_clean > 100 ) {
            i_clean = 100;
        }
        if ( i_clean != oi_progress ) {
            int i_old = oi_progress;
            oi_progress = i_clean;
            osupport.firePropertyChange("progress", i_old, i_clean);
        }
    }

    public void setProgress(int i_progress, int i_max) {
        setProgress( (int) ((float) i_progress / (float) i_max) );
    }

    private String os_title = "";

    public String getTitle() {
        return os_title;
    }

    public void setTitle(String s_title) {
        String s_old = os_title;
        os_title = s_title;
        osupport.firePropertyChange( "title", s_old, s_title );
    }

    public void publish( Object x_result ) {
        osupport.fireLittleEvent( new UiPublishEvent( this, x_result ) );
    }
    
    public void log(Level level, String s_info) {
        osupport.fireLittleEvent( new UiMessageEvent( this, level, s_info ) );
    }

    public void info(String s_info) {
        this.log( Level.INFO, s_info );
    }

    public void addLittleListener(LittleListener listen_action) {
        osupport.addLittleListener( listen_action );
    }

    public void removeLittleListener(LittleListener listen_action) {
        osupport.removeLittleListener( listen_action );
    }

    public void addPropertyChangeListener(PropertyChangeListener listen_props) {
        osupport.addPropertyChangeListener( listen_props );
    }

    public void removePropertyChangeListener(PropertyChangeListener listen_props) {
        osupport.removePropertyChangeListener( listen_props );
    }

}
