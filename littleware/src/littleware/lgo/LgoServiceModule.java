/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.lgo;

import java.util.Collection;
import littleware.bootstrap.SessionModule;

/**
 * Marker interface for AppModule and Session Module bootstrap modules
 * that allows auto-registration of lgo commands with the lgo command dictionary.
 */
public interface LgoServiceModule extends SessionModule {
    public Collection<Class<? extends LgoCommand.LgoBuilder>> getLgoCommands();
}
