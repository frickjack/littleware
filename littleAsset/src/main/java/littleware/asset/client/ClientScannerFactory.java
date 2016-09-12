package littleware.asset.client;

import com.google.inject.Provider;
import littleware.asset.TemplateScanner;

/**
 * Factory for client-side AssetSearchManager based
 * template scanner
 */
public interface ClientScannerFactory extends Provider<TemplateScanner> {

}
