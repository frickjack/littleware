/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.net;

import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Future;
import littleware.base.Option;
import littleware.base.cache.CacheableObject;

/**
 * Support for asynchronous remote method calls
 */
public interface LittleResponse<T> extends CacheableObject, Future<T> {
    public enum State {
        Redirect, Processing, Complete, Failed;
    }

    public URL getFollowUpServer();
    public State getState();
    public Option<T> getResult();
    public Option<Throwable>  getError();
    public int getProgress();
    public Collection<String> getMessageIds();
}
