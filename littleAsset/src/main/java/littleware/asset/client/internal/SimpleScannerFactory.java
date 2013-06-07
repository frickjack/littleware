/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import littleware.asset.TemplateScanner;
import littleware.asset.TreeNode;
import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.ClientScannerFactory;
import littleware.asset.internal.AbstractTemplateScanner;
import littleware.base.Options;
import littleware.base.Option;

@Singleton
public class SimpleScannerFactory implements ClientScannerFactory {
    private final AssetSearchManager search;

    @Inject
    public SimpleScannerFactory( AssetSearchManager search ) {
        this.search = search;
    }

    @Override
    public TemplateScanner get() {
        return new Scanner( search );
    }

    private static final Option<TreeNode> emptyNode = Options.empty();

    //------------------------------------------
    public static class Scanner extends AbstractTemplateScanner {

        private final AssetSearchManager search;

        public Scanner(AssetSearchManager search) {
            this.search = search;
        }

        @Override
        protected Option<TreeNode> loadAsset(UUID parentId, String name) {
            try {
                final AssetRef ref = search.getAssetFrom( parentId, name );
                if ( ref.isSet() ) {
                    return Options.some( ref.get().narrow( TreeNode.class ) );
                } else {
                    return emptyNode;
                }
            } catch ( RuntimeException ex ) {
                throw ex;
            } catch ( Exception ex ) {
                throw new IllegalStateException( "Exception loading tree node at " + parentId + "/" + name, ex );
            }
        }
    }
}
