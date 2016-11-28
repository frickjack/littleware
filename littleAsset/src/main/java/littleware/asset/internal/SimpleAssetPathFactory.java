package littleware.asset.internal;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.UUID;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.asset.LittleHome;
import littleware.base.ParseException;
import littleware.base.UUIDFactory;

/**
 * Source of AssetPath objects.
 */
@Singleton
public class SimpleAssetPathFactory implements AssetPathFactory {

    private static final Logger log = Logger.getLogger(SimpleAssetPathFactory.class.getName());


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
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Invalid asset type in path: " + pathIn);
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
        final List<String> v_parts = new ArrayList<>();
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

}
