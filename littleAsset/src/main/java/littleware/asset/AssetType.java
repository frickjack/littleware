package littleware.asset;

import java.util.*;
import java.util.logging.Logger;

import littleware.base.*;

/**
 * Enumerate different types of assets, and provide
 * factory methods to create appropriate (uninitialized)
 * Asset specialization type.
 * We implement our own Enum pattern, since we want
 * to be able to dynamically add members to the Enum.
 * A user should be able to introduce a new AssetType to the
 * system by registering a new entry in the database,
 * and adding code to the classpath that defines the
 * AssetType and AssetSpecializer for that new type.
 * Our enum's are uniquely identified by UUID, not by
 * integer ordinal - so we don't have to worry about 
 * 2 users in different locations assigning the same ID to 
 * 2 different types.
 * Subtypes should attempt to maintain themselves as singletons locally,
 * but also tolerate the serialization/deserialization of new instances
 * due to RPC/etc.
 *
 * Note: subtypes implementing AssetType must be in a code-base
 *     granted AccessPermission "littleware.asset.resource.newtype"
 */
public class AssetType extends DynamicEnum<AssetType> {

    private static final long serialVersionUID = 1111142L;
    private static final Logger log = Logger.getLogger(AssetType.class.getName());
    private static final Map<AssetType, Set<AssetType>> subtypeIndex = new HashMap<>();

    private static void updateSubtypes(AssetType newType) {
        synchronized (subtypeIndex) {
            subtypeIndex.put(newType, new HashSet<>());
            for (AssetType parent = newType.getSuperType().orElse(null); null != parent;
                    parent = parent.getSuperType().orElse(null)) {
                subtypeIndex.get(parent).add(newType);
            }
        }
    }

    /**
     * Little utility returns the subtypes of the given parent type that
     * have been instantiated (and auto-registered) so far ...
     * 
     * @return set of subtypes - may be empty
     */
    public static Set<AssetType> getSubtypes(AssetType parent) {
        synchronized (subtypeIndex) {
            return Collections.unmodifiableSet(subtypeIndex.get(parent));
        }
    }
    
    private Optional<AssetType> superType = Optional.empty();

    /**
     * Do-nothing constructor intended for deserialization only.
     */
    protected AssetType() {
        //final Permission permission = new AccessPermission("newtype");
        //AccessController.checkPermission(permission);
        updateSubtypes( this );
    }

    /**
     * Constructor for subtypes to register a u_id/s_name
     * for the default implementation of getObjectId() and getName()
     */
    public AssetType(UUID typeId, String typeName) {
        super(typeId, typeName, AssetType.class);
        updateSubtypes( this );
    }

    public AssetType(UUID typeId, String typeName, AssetType superType) {
        super(typeId, typeName, AssetType.class);
        this.superType = Optional.ofNullable(superType);
        updateSubtypes( this );
    }

    /** Shortcut to DynamicEnum.getMembers */
    public static Set<AssetType> getMembers() {
        return getMembers(AssetType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static AssetType getMember(UUID id) throws NoSuchThingException {
        return getMember(id, AssetType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static AssetType getMember(String name) throws NoSuchThingException {
        return getMember(name, AssetType.class);
    }

    /**
     * Does littleware enforce name-uniqueness (on a given server cluster) 
     * for this asset-type ?
     * A false result indicates that two assets
     * of this type may have the same name.
     * This base class method returns:
     *         (getSuperType().isEmpty()) ? false : getSuperType ().isNameUnique ()
     *
     * @return true if each asset of this type has a unique name within the
     *          set of assets of this type within a single littleware asset repository.
     */
    public boolean isNameUnique() {
        return (getSuperType().isPresent()) ? getSuperType().get().isNameUnique() : false;
    }
    
    /**
     * Does the timestamp on the node indicate a consistent client-side cache ?
     * True for simple assets, but may be false for compound assets like a Group -
     * where a Group's node may not have changed, but one of its subgroups is changed.
     */
    public boolean isTStampCache() { return true; }

    /**
     * Allow an asset-type to have one super type.
     * Methods that search based on AssetType, like AssetSearchManager.getByName,
     * will return objects assets matching the given type and
     * any of its subtypes.
     *
     * @return super-type, or null if no super-type (this implementation returns null)
     */
    public final Optional<AssetType> getSuperType() {
        return superType;
    }

    /**
     * Is this asset-type a subtype of the given asset-type ?
     * Note that if x isNameUnique, then all subtypes of x must
     * by nameUnique too - or you'll run into problems.
     *
     * NOTE: AssetTypes do not share a common ancestor (like Object) -
     *     most search routines just use NULL as a wildcard asset-type
     */
    public final boolean isA(AssetType atype_other) {
        if ((null == this) || (null == atype_other)) {
            return false;
        }

        // climb this assset's inheritance tree, cache the result
        for (Optional<AssetType> maybe = Optional.of((AssetType) this);
                maybe.isPresent();
                maybe = maybe.get().getSuperType()) {
            if (maybe.get().equals(atype_other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a user must be in the ADMIN group to create this type of asset.
     * This base class method returns:
     *         (null == getSuperType()) ? false : getSuperType ().isAdminToCreate ()
     */
    public boolean isAdminToCreate() {
        return (getSuperType().isPresent()) ? getSuperType().get().isAdminToCreate() : false;
    }
    /**
     * UNKNOWN asset-type - place holder for asset-types that we don't have a handler for.
     * The engine refuses to save/create/update assets with UNKNOWN type.
     */
    public static final AssetType UNKNOWN = new AssetType(UUIDFactory.parseUUID("EDC97D5F816044E69BFC289F4715BA45"),
            "littleware.UNKNOWN");
}
