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

package littleware.apps.lgo;

/**
 * Simple POJO
 */
public class EzLgoExample implements LgoExample {

    private final String os_title;
    public String getTitle() {
        return os_title;
    }

    private final String os_description;
    public String getDescription() {
        return os_description;
    }

    /**
     * Constructor injects parameter values.
     */
    public EzLgoExample( String s_title, String s_description ) {
        os_title = s_title;
        os_description = s_description;
    }
}
