/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.servlet;

import com.google.inject.Inject;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.apps.misc.ThumbManager;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.Maybe;

/**
 * Simple LittleServlet serves out thumbnail images
 */
public class ThumbServlet extends LittleServlet {
    private ThumbManager thumbManager;
    private AssetSearchManager search;
    private AssetPathFactory pathFactory;

    @Inject
    public void injectMe( ThumbManager thumbManager, AssetSearchManager search,
            AssetPathFactory pathFactory
            ) {
        this.thumbManager = thumbManager;
        this.search = search;
        this.pathFactory = pathFactory;
    }

    @Override
    public void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String pathString = request.getParameter("path" );
        UUID  id = UUID.randomUUID();
        try {
            if ( null != pathString ) {
                final Maybe<Asset> maybe = search.getAssetAtPath( pathFactory.createPath(pathString));
                if ( maybe.isSet() ) {
                    id = maybe.get().getId();
                }
            }
            final String mimeType = "image/jpeg";
            response.setContentType(mimeType);
            final ThumbManager.Thumb thumb = thumbManager.loadThumb( id );
            final ImageWriter writer = ImageIO.getImageWritersByMIMEType( mimeType ).next();
            writer.setOutput( ImageIO.createImageOutputStream( response.getOutputStream() ) );
            writer.write( (RenderedImage) thumb.getThumb() );
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new ServletException( "Failed to retrieve thumbnail for " + pathString, ex );
        }
    }

}
