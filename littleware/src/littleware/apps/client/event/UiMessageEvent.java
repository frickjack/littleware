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

package littleware.apps.client.event;

import java.util.logging.Level;
import littleware.apps.client.LittleEvent;
import littleware.apps.client.UiFeedback;

/**
 * LittleEvent fired by UiFeedback implementation on log() call.
 */
public class UiMessageEvent extends LittleEvent {

    private final String os_message;
    private final Level  olevel;

    /**
     * Event triggered when worker call UiFeedback.log
     *
     * @param source
     * @param level of the message
     * @param s_message sent to UiFeedback
     */
    public UiMessageEvent( UiFeedback source, Level level, String s_message ) {
        super( source, "UiMessageEvent", s_message );
        os_message = s_message;
        olevel = level;
    }

    public String getMessage() { return os_message; }
    public Level  getLevel () { return olevel; }
}
