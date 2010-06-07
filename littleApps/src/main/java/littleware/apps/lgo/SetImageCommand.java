/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import littleware.apps.image.ImageManager;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Set the image associated with some asset.
 * Just returns true on success - throws
 * exception on failure conditions.
 *
 *      setimage -path [asset path] -image [image-file]
 */
public class SetImageCommand extends AbstractLgoCommand<String,Boolean> {
    private final AssetSearchManager osearch;
    private final ImageManager omgrImage;
    private final AssetPathFactory ofactoryPath;

    @Inject
    public SetImageCommand ( AssetSearchManager search,
            ImageManager mgrImage,
            AssetPathFactory factoryPath
            ) {
        super( SetImageCommand.class.getName() );
        osearch = search;
        omgrImage = mgrImage;
        ofactoryPath = factoryPath;
    }
    
    private enum Option { path, image; 
        
        public static Map<String,String> getMap()
        {
            final Map<String,String> mapTemp = new HashMap<String,String>();
            mapTemp.put( path.toString(), "" );
            mapTemp.put( image.toString(), "" );
            return mapTemp;
        }
    }
    
    /**
     * Assign an image to an asset - both specified by
     * the command args - sPath provides default asset-path.
     * 
     * @param feedback
     * @param sDefaultPath default asset-path if not given by args
     * @return Boolean.True on success
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public Boolean runSafe(Feedback feedback, String sDefaultPath ) throws LgoException {
        final Map<String,String> mapOption = Option.getMap();
        mapOption.put( Option.path.toString(), sDefaultPath );
        final Map<String,String> mapArgs = processArgs( mapOption, getArgs() );
        for ( Option option : Option.values() ) {
            if ( Whatever.get().empty( mapArgs.get( option.toString() ) )) {
                throw new LgoArgException( "missing required arg: " + option );
            }
        }
        final String sImage = mapArgs.get(Option.image.toString() );
        final BufferedImage image;
        try {
            image = ImageIO.read(new File( sImage ));
        } catch (IOException ex) {
            throw new LgoArgException( "Unable to load image: " + sImage, ex );
        }
        if ( null == image ) {
            throw new LgoArgException( "Unable to handle image: " + sImage );
        }
        final String sPath = mapArgs.get( Option.path.toString() );
        final AssetPath path;
        try {
            path = ofactoryPath.createPath(mapArgs.get(Option.path.toString()));
        } catch (BaseException ex) {
            throw new LgoArgException( "Unable to parse path: " + sPath, ex );
        }
        final Asset aImage;
        try {
           aImage = osearch.getAssetAtPath(path).get();
        } catch ( Exception ex ) {
            throw new LgoArgException( "Unable to load asset at path: " + path, ex );
        }
        try {
            omgrImage.saveImage(aImage, image, "SetImageCommand" );
        } catch ( Exception ex ) {
            throw new LgoException( "Failed to asssign image to path: " + path, ex );
        }
        return Boolean.TRUE;
    }
}
