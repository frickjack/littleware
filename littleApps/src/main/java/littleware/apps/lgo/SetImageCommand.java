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

import com.google.common.collect.ImmutableMap;
import java.util.List;
import littleware.lgo.AbstractLgoCommand;
import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
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
import littleware.lgo.AbstractLgoBuilder;

/**
 * Set the image associated with some asset.
 * Just returns true on success - throws
 * exception on failure conditions.
 *
 *      setimage -path [asset path] -image [image-file]
 */
public class SetImageCommand extends AbstractLgoCommand<SetImageCommand.Input, Boolean> {

    public static class Input {

        private final AssetPath path;
        private final File imageFile;

        public Input(AssetPath path, File imageFile) {
            this.path = path;
            this.imageFile = imageFile;
        }

        public File getImageFile() {
            return imageFile;
        }

        public AssetPath getPath() {
            return path;
        }
    }

    public static class Builder extends AbstractLgoBuilder<Input> {

        private final AssetSearchManager search;
        private final ImageManager imageMgr;
        private final AssetPathFactory pathFactory;

        private enum Option {

            path, image;
            private static final Map<String, String> optionMap = ImmutableMap.of(path.toString(), "",
                    image.toString(), "");

            public static Map<String, String> getMap() {
                return optionMap;
            }
        }

        @Inject
        public Builder(AssetSearchManager search,
                ImageManager imageMgr,
                AssetPathFactory pathFactory) {
            super(SetImageCommand.class.getName());
            this.search = search;
            this.imageMgr = imageMgr;
            this.pathFactory = pathFactory;
        }

        @Override
        public SetImageCommand buildSafe(Input input ) {
            return new SetImageCommand( search, imageMgr, input );
        }

        @Override
        public SetImageCommand buildFromArgs(List<String> args) {
            final Map<String, String> optionMap = Option.getMap();
            final Map<String, String> mapArgs = processArgs(args, optionMap);
            for (Option option : Option.values()) {
                if (Whatever.get().empty(mapArgs.get(option.toString()))) {
                    throw new IllegalArgumentException("missing required arg: " + option);
                }
            }
            final String sImage = mapArgs.get(Option.image.toString());
            final String sPath = mapArgs.get(Option.path.toString());
            final AssetPath path;
            try {
                path = pathFactory.createPath(mapArgs.get(Option.path.toString()));
            } catch (BaseException ex) {
                throw new IllegalArgumentException("Unable to parse path: " + sPath, ex);
            }
            return buildSafe(new Input(path, new File(sImage)));
        }
    }
    private final AssetSearchManager search;
    private final ImageManager imageMgr;

    @Inject
    public SetImageCommand(AssetSearchManager search,
            ImageManager mgrImage,
            Input input) {
        super(SetImageCommand.class.getName(), input);
        this.search = search;
        this.imageMgr = mgrImage;
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
    public Boolean runCommand(Feedback feedback) throws Exception {
        final Input input = getInput();
        final BufferedImage image = ImageIO.read(input.getImageFile());
        if (null == image) {
            throw new IllegalArgumentException("Unable to handle image: " + input.getImageFile());
        }
        final Asset aImage = search.getAssetAtPath(input.getPath()).get();
        imageMgr.saveImage(aImage, image, "SetImageCommand");
        return Boolean.TRUE;
    }
}
