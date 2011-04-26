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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.apps.image.ImageManager;
import littleware.apps.image.ThumbManager;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.Maybe;
import littleware.security.LittleUser;

/**
 * Simple LittleServlet serves out thumbnail images
 */
public class ThumbServlet extends LittleServlet {

    private static final Logger log = Logger.getLogger(ThumbServlet.class.getName());
    private ImageManager imageMgr;
    private AssetSearchManager search;
    private AssetPathFactory pathFactory;
    private ThumbManager thumbMgr;
    private LittleUser user;

    @Inject
    public void injectMe(ImageManager imageMgr, AssetSearchManager search,
            AssetPathFactory pathFactory, ThumbManager thumbMgr, LittleUser user) {
        this.imageMgr = imageMgr;
        this.search = search;
        this.thumbMgr = thumbMgr;
        this.pathFactory = pathFactory;
        this.user = user;
    }

    @Override
    public void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String pathString = request.getParameter("path");
        final String resString = request.getParameter("res");
        UUID id = user.getId();
        ImageManager.SizeOption size = ImageManager.SizeOption.r128x128;
        try {
            if (null != pathString) {
                final Option<Asset> maybe = search.getAssetAtPath(pathFactory.createPath(pathString));
                if (maybe.isSet()) {
                    id = maybe.get().getId();
                }
            }
            if (null != resString) {
                try {
                    final int res = Integer.parseInt(resString);
                    for (ImageManager.SizeOption scan : ImageManager.SizeOption.values()) {
                        if (scan.getWidth() > res) {
                            size = scan;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    log.log(Level.INFO, "Failed to parse resolution parameter: " + resString, ex);
                }
            }
            final String mimeType = "image/png";
            response.setContentType(mimeType);
            final BufferedImage image = imageMgr.loadImage(id, ImageManager.SizeOption.r128x128).getOr(thumbMgr.getDefault().getThumb());
            final ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            writer.setOutput(ImageIO.createImageOutputStream(response.getOutputStream()));
            writer.write( image);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException("Failed to retrieve thumbnail for " + pathString, ex);
        }
    }
}
