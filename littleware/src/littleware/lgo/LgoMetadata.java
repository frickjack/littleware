/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo;

/**
 * Metadata about and LGO command provided by
 * LgoModule at module-load time.
 * The metadata can help determine the bootstrap runtime
 * environment to setup to execute a command.
 */
public interface LgoMetadata {

    public Class<? extends LgoCommand> getCommandClass();
}
