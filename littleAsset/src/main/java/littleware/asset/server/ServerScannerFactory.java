/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server;

import littleware.asset.TemplateScanner;

/**
 * Factory for server-side ServerSearchManager based
 * template scanner
 */
public interface ServerScannerFactory {
    public TemplateScanner build( LittleContext ctx );
}
