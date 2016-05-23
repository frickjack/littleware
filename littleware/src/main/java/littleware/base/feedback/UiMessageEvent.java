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
import littleware.base.event.LittleEvent;

/**
 * LittleEvent fired by Feedback implementation on log() call.
 */
public class UiMessageEvent extends LittleEvent {

    private final String message;
    private final Level  level;

    /**
     * Event triggered when worker call Feedback.log
     *
     * @param source
     * @param level of the message
     * @param message sent to Feedback
     */
    public UiMessageEvent( Feedback source, Level level, String message ) {
        super( source );
        this.message = message;
        this.level = level;
    }

    public String getMessage() { return message; }
    public Level  getLevel () { return level; }
}
