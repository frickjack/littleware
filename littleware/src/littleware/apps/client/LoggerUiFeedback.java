/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.client;

import littleware.apps.client.NullFeedback;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.client.event.UiMessageEvent;

/**
 * Simple logger-based implementation of UiFeedback interface.
 * Just adds listeners that log each UIFeedback event.
 */
public class LoggerUiFeedback extends NullFeedback {
    private Logger olog = Logger.getLogger( LoggerUiFeedback.class.getName() );

    {
        this.addPropertyChangeListener( new PropertyChangeListener () {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ( evt.getPropertyName().equalsIgnoreCase( "progress" ) ) {
                    olog.log( Level.INFO, getTitle() + ": " + getProgress() + "%" );
                }
            }
        }
        );

        this.addLittleListener( new LittleListener() {
            @Override
            public void receiveLittleEvent(LittleEvent event_little) {
                if ( event_little instanceof UiMessageEvent ) {
                    olog.log( Level.INFO, event_little.getResult().toString() );
                }
            }
        }
        );
    }

    /** Just use general logger */
    public LoggerUiFeedback() {}
    /** Inject logger */
    public LoggerUiFeedback( Logger logger ) {
        olog = logger;
    }
}
