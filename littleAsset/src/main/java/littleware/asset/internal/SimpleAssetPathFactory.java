/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;

import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.NoSuchThingException;
import littleware.base.ParseException;
import littleware.base.UUIDFactory;

/**
 * Source of AssetPath objects.
 */
@Singleton
public class SimpleAssetPathFactory implements AssetPathFactory {

    private static final Logger log = Logger.getLogger(SimpleAssetPathFactory.class.getName());
    private final AssetSearchManager search;

    /** Injecting constructor */
    @Inject
    public SimpleAssetPathFactory(
            AssetSearchManager search) {
        this.search = search;
    }

    @Override
    public AssetPath createPath( final String pathIn) throws AssetException, ParseException {
        String rootedPath = pathIn;
        if (!rootedPath.startsWith("/")) {
            rootedPath = "/" + pathIn;
        }
        int firstSlash = rootedPath.indexOf("/", 1);
        final String rootStr;
        final String subrootStr;

        if (firstSlash > 0) {
            // Then there's a path part
            rootStr = rootedPath.substring(1, firstSlash);
            subrootStr = rootedPath.substring(firstSlash);
        } else {
            rootStr = rootedPath.substring(1);
            subrootStr = "";
        }

        // byname AssetPath - default type littleware.HOME, default name s_root
        String assetName = rootStr;
        AssetType assetType = LittleHome.HOME_TYPE;
        final int typeTokenIndex = rootStr.indexOf(":type:");

        try {
            if (rootStr.startsWith(AssetPathFactory.PathRootPrefix.ById.toString())) {
                try {
                    final UUID rootId = UUIDFactory.parseUUID(rootStr.substring(AssetPathFactory.PathRootPrefix.ById.toString().length()));
                    return createPath(rootId, subrootStr);
                } catch (IllegalArgumentException ex) {
                    throw new ParseException("Invalid uuid: " + rootStr, ex);
                }
            } else if (rootStr.startsWith(AssetPathFactory.PathRootPrefix.ByName.toString())) {
                // user specified name
                final int nameLength = AssetPathFactory.PathRootPrefix.ByName.toString().length();

                if (typeTokenIndex < 0) {
                    assetName = rootStr.substring(nameLength);
                } else {
                    assetName = rootStr.substring(nameLength, typeTokenIndex);
                    assetType = AssetType.getMember(rootStr.substring(typeTokenIndex + ":type:".length()));
                }
            } else if (typeTokenIndex > 0) {
                // user did not specify name, but specified a type
                assetType = AssetType.getMember(rootStr.substring(typeTokenIndex + ":type:".length()));
            } else {
                // user specified nothing
                // check to make sure the name is not a UUID
                try {
                    return createPath(UUIDFactory.parseUUID(assetName), subrootStr);
                } catch (IllegalArgumentException ex) {
                    log.log(Level.FINE, "Unspecified path is not a UUID path");
                }
            }
            return createPath(assetName, assetType, subrootStr);
        } catch (NoSuchThingException ex) {
            throw new IllegalArgumentException("Invalid asset type in path: " + pathIn);
        }
    }

    @Override
    public AssetPath createPath(String rootName, AssetType rootType,
            String subRootPath) throws ParseException, InvalidAssetTypeException {
        return new SimpleAssetPathByRootName(rootType, rootName,
                subRootPath, this);
    }

    @Override
    public AssetPath createPath(UUID rootId, String subRootPath) throws ParseException {
        return new SimpleAssetPathByRootId(rootId,
                subRootPath, this);
    }

    @Override
    public AssetPath createPath(UUID u_root) {
        return new SimpleAssetPathByRootId(u_root, "", this);
    }

    @Override
    public String cleanupPath(final String path) {
        final List<String> v_parts = new ArrayList<String>();
        log.log(Level.FINE, "Processing path: {0}", path);

        for (StringTokenizer toker = new StringTokenizer(path, "/");
                toker.hasMoreTokens();) {
            final String token = toker.nextToken();
            log.log(Level.FINE, "Processing token: {0}", token);
            if (token.equals(".")) {
                continue;
            }
            if (token.equals("..")
                    && (v_parts.size() > 1)
                    && (!v_parts.get(v_parts.size() - 1).equals(".."))) {
                // remove last part
                log.log(Level.FINE, "Got .., removing: {0}", v_parts.remove(v_parts.size() - 1));
                continue;
            }
            log.log(Level.FINE, "Adding token to token list: {0}", token);
            v_parts.add(token);
        }

        String s_result = "/";
        if (!v_parts.isEmpty()) {
            StringBuilder sb_result = new StringBuilder(256);
            for (String s_part : v_parts) {
                sb_result.append("/");
                sb_result.append(s_part);
            }
            s_result = sb_result.toString();
        }
        return s_result;
    }

    @Override
    public AssetPath normalizePath( final AssetPath pathIn) throws BaseException, AssetException, GeneralSecurityException,
            RemoteException {
        String normalStr = pathIn.getSubRootPath();
        if (normalStr.startsWith("..")) { // just in case
            normalStr = "/" + normalStr;
        }
        if (!normalStr.startsWith("/..")) {
            return pathIn;
        }
        Asset rootAsset = pathIn.getRoot(search).get();
        for (;
                normalStr.startsWith("/..");
                normalStr = normalStr.substring(3)) {
            TreeNode treeNode = rootAsset.narrow();
            final Option<Asset> maybeParent = search.getAsset(treeNode.getParentId());
            if (!maybeParent.isSet()) {
                throw new IllegalArgumentException("Unable to normalize path for " + pathIn);
            }
            rootAsset = maybeParent.get();
        }
        return createPath(rootAsset.getId(), normalStr);
    }

    @Override
    public AssetPath toRootedPath(AssetPath pathIn) throws BaseException, GeneralSecurityException,
            RemoteException {
        final AssetPath pathNormal = normalizePath(pathIn);
        final Option<Asset> maybeRoot = pathNormal.getRoot(search);
        if ( (!maybeRoot.isSet()) || (! (maybeRoot.get() instanceof TreeParent)) ) {
            return pathNormal;
        }
        final List<Asset> assetTrail = new ArrayList<Asset>();
        assetTrail.add(maybeRoot.get());
        for (Option<Asset> maybeParent = search.getAsset(maybeRoot.get().getFromId());
                maybeParent.isSet();
                maybeParent = search.getAsset(maybeParent.get().getFromId())) {
            assetTrail.add(maybeParent.get());
        }
        Collections.reverse(assetTrail);
        final StringBuilder sbSubrootPath = new StringBuilder();
        boolean bFirst = true;
        for (Asset aPart : assetTrail) {
            if (bFirst) {
                // skip the root
                bFirst = false;
                continue;
            }
            sbSubrootPath.append("/").append(aPart.getName());
        }
        sbSubrootPath.append("/").append(pathNormal.getSubRootPath());
        final Asset aRoot = assetTrail.get(0);
        if (aRoot.getAssetType().isNameUnique()) {
            return normalizePath(createPath(aRoot.getName(), aRoot.getAssetType(), sbSubrootPath.toString()));
        } else {
            return normalizePath(createPath(aRoot.getId(), sbSubrootPath.toString()));
        }
    }

    @Override
    public AssetPath toRootedPath(UUID assetId) throws BaseException, GeneralSecurityException, RemoteException {
        return toRootedPath(createPath(assetId));
    }
}
