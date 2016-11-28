package littleware.asset.internal;

import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.base.ParseException;


/**
 * Convenience baseclass for AssetPath implementations 
 */
public abstract class AbstractAssetPath implements AssetPath {

    private static final Logger log = Logger.getLogger("littleware.asset.AbstractAssetPath");
    private final String subrootPath;
    private final String path;
    private final AssetPathFactory pathFactory;
    
    /**
     * Constructor stashes the string path after processing
     * it through AssetFactory.cleanupPath
     *
     * @param path of form /ROOT/A/B/C or whatever
     */
    protected AbstractAssetPath(String path, AssetPathFactory pathFactory) {
        this.path = pathFactory.cleanupPath(path);
        int i_slash = path.indexOf("/", 1);
        if (i_slash < 0) {
            subrootPath = "";
        } else {
            subrootPath = path.substring(i_slash);
        }
        this.pathFactory = pathFactory;
    }

    @Override
    public boolean hasRootBacktrack() {
        return subrootPath.startsWith("/..");
    }

    @Override
    public String getBasename() {
        final int slashIndex = path.lastIndexOf('/');
        if (slashIndex >= 0) {
            if (slashIndex + 1 < path.length()) {
                return path.substring(slashIndex + 1);
            } else {
                return "";
            }
        } else {
            return path;
        }
    }


    @Override
    public String getSubRootPath() {
        return subrootPath;
    }


    @Override
    public boolean hasParent() {
        return subrootPath.matches("^(/\\.\\.)*/.?[^\\.].*$");
    }

    /**
     * Relies on clone to assemble the parent path if hasParent(),
     * otherwise just returns this.
     */
    @Override
    public AssetPath getParent() {
        if (hasParent()) {
            try {
                return pathFactory.createPath( path.substring(0, path.lastIndexOf("/")) );
            } catch (AssetException | ParseException ex) {
                log.log(Level.SEVERE, "Internal error parsing parent path of " + path, ex);
                throw new IllegalStateException( "Error parsing parent of " + path, ex );
            }
        } else {
            return this;
        }
    }

    /** Just return constructor-supplied path string */
    @Override
    public String toString() {
        return path;
    }

    /** Just hash on toString() */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /** Just compare on toString () */
    @Override
    public int compareTo(AssetPath other) {
        return path.compareTo(other.toString());
    }

    
    @Override
    public boolean equals(Object other) {
        return ((other instanceof AbstractAssetPath) && other.toString().equals(this.toString()));
    }
}
