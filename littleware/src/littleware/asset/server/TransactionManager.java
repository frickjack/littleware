/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server;


/**
 * Just an injector that allows a client to get
 * a reference to the LittleTransaction active on
 * the current thread stack.
 */
public interface TransactionManager {

    /**
     * Get the LittleTransaction active on the calling thread.
     */
    public LittleTransaction getThreadTransaction ();        
}


