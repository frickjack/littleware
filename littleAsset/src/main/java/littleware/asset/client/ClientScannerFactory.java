/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.client;

import com.google.inject.Provider;
import littleware.asset.TemplateScanner;

/**
 * Factory for client-side AssetSearchManager based
 * template scanner
 */
public interface ClientScannerFactory extends Provider<TemplateScanner> {

}
