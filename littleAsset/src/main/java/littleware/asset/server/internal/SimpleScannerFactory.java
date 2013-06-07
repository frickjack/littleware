/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.internal;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.TemplateScanner;
import littleware.asset.TreeNode;
import littleware.asset.internal.AbstractTemplateScanner;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerScannerFactory;
import littleware.asset.server.ServerSearchManager;
import littleware.base.Options;
import littleware.base.Option;

@Singleton
public class SimpleScannerFactory implements ServerScannerFactory {


    private static final Option<TreeNode> emptyNode = Options.empty();
    private final ServerSearchManager search;

    @Inject
    public SimpleScannerFactory( ServerSearchManager search ) {
        this.search = search;
    }

    @Override
    public TemplateScanner build(LittleContext ctx) {
        return new Scanner( ctx, search );
    }

    //------------------------------------------
    public static class Scanner extends AbstractTemplateScanner {
        private final LittleContext ctx;

        private final ServerSearchManager search;

        public Scanner( LittleContext ctx, ServerSearchManager search) {
            this.ctx = ctx;
            this.search = search;
        }

        @Override
        protected Option<TreeNode> loadAsset(UUID parentId, String name) {
            try {
                final Option<Asset> maybe = search.getAssetFrom( ctx, parentId, name );
                if ( maybe.isSet() ) {
                    return Options.some( maybe.get().narrow( TreeNode.class ) );
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
