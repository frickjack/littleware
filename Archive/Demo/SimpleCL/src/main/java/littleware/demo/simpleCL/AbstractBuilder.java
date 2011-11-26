/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.demo.simpleCL;

import com.google.inject.internal.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Just implement an argv property for AppBuilder implementations
 */
public abstract class AbstractBuilder implements AppBuilder {
    private List<String> argv = Collections.emptyList();

    protected List<String> getArgv() {
        return argv;
    }
    
    @Override
    public AppBuilder argv(Collection<String> value) {
        this.argv = ImmutableList.copyOf( value );
        return this;
    }


}
