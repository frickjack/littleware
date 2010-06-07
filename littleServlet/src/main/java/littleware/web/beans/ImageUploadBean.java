/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.beans;

import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import littleware.apps.misc.ImageManager;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.Maybe;
import littleware.security.LittleUser;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 * Manage upload of png image and association with asset
 */
@ManagedBean
@SessionScoped
public class ImageUploadBean extends InjectMeBean {
    private static final Logger log = Logger.getLogger( ImageUploadBean.class.getName() );
    private String path;
    private BufferedImage image;
    private ImageManager imageMgr;
    private AssetPathFactory pathFactory;
    private AssetSearchManager search;
    private String imageName = "";

    /**
     * Path to asset to associate image with
     */
    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        path = value;
    }

    public String getImageName() {
        return imageName;
    }

    @Inject
    public void injectMe(ImageManager imageMgr, AssetPathFactory pathFactory,
            AssetSearchManager search, LittleUser user) {
        this.imageMgr = imageMgr;
        this.pathFactory = pathFactory;
        this.search = search;
        this.path = "/" + user.getId();
    }

    /**
     * Process the AJAX file-upload
     */
    public void handleUpload(FileUploadEvent event) {
        try {
            final UploadedFile file = event.getFile();
            log.log( Level.FINE, "Uploaded file: " + file.getFileName() + ", " + path );
            this.image = ImageIO.read( new ByteArrayInputStream( file.getContents() ) );
            if ( null == image ) {
                throw new IllegalStateException( "Image is null! ..." );
            }
            imageName = file.getFileName();
            final FacesMessage msg = new FacesMessage("Successful", file.getFileName() + " uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception ex) {
            final FacesMessage msg = new FacesMessage("Error", event.getFile().getFileName() + " upload failed: " + ex);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            imageName = "";
        }
    }

    public String processImage() {
        final String failed = "imageUploadFailed";
        try {
            final Maybe<Asset> maybe = search.getAssetAtPath(pathFactory.createPath(path));
            if (maybe.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage("BadPath", "Failed to load asset at " + path));
                return failed;
            }
            if ( null == image ) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage("BadImage", "Null image at save time" ));
                return failed;
            }
            imageMgr.saveImage(maybe.get(),
                    image, "Web image upload"
                    );
            return "imageUploadOk";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Error", "failed to associate image with " + path + ": " + ex));
            return failed;
        }
    }
}
