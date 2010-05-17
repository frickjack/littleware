/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap;

import java.util.Collection;

/**
 * Bootstrap manager for applications that use
 * some littleware utilities but do not access the
 * littleware node database as a client or implement
 * a littleware server service.
 */
public interface AppBootstrap {

    public enum AppConfig {
        SwingApp, CliApp;
    }

    public Collection<AppModule>  getModuleList();
}
