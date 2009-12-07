/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;



/**
 * Base exception of littlego command tool.
 */
public class LgoBadArgumentException extends LgoException {
    private static final long serialVersionUID = 7358268103700053539L;

    public LgoBadArgumentException () {
        super();
    }
    
    public LgoBadArgumentException( String s_message ) {
        super( s_message );
    }
    
    public LgoBadArgumentException( String s_message, Throwable e_cause ) {
        super( s_message, e_cause );
    }
    
}
