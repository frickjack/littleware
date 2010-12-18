/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.feedback;

import java.util.logging.Level;
import littleware.base.feedback.LittleEvent;

/**
 * LittleEvent fired by Feedback implementation on log() call.
 */
public class UiMessageEvent extends LittleEvent {

    private final String os_message;
    private final Level  olevel;

    /**
     * Event triggered when worker call Feedback.log
     *
     * @param source
     * @param level of the message
     * @param s_message sent to Feedback
     */
    public UiMessageEvent( Feedback source, Level level, String s_message ) {
        super( source, "UiMessageEvent", s_message );
        os_message = s_message;
        olevel = level;
    }

    public String getMessage() { return os_message; }
    public Level  getLevel () { return olevel; }
}
