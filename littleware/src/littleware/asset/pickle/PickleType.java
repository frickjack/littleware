package littleware.asset.pickle;

import java.util.*;
import java.util.logging.Logger;
import java.security.AccessController;
import java.security.Permission;


import littleware.base.*;
import littleware.asset.*;

/**
 * Enumerate different types of pickle makers, and provide
 * factory methods to create appropriate PickleMakers.
 * Do some dynamic loading here to fascilitate registering
 * PickleMakers for new user-defined asset types and classes.
 *
 * Note: subtypes implementing PickleType must be in a code-base
 *     granted AccessPermission "littleware.security.resource.newpickle"
 */
public abstract class PickleType extends DynamicEnum<PickleType> {

    private static Logger olog_generic = Logger.getLogger(PickleType.class.getName());

    /**
     * Do-nothing constructor intended for deserialization only.
     * Does a permission-check against littleware.security.resource.newpickle -
     * make sure some foreign-code frickjack isn't inserting some code on us.
     */
    protected PickleType() {
        Permission perm_newpickle = new AccessPermission("newpickle");
        AccessController.checkPermission(perm_newpickle);
    }

    /**
     * Constructor for subtypes to register a u_id/s_name
     * for the default implementation of getObjectId() and getName()
     */
    protected PickleType(UUID u_id, String s_name) {
        super(u_id, s_name, PickleType.class, new AccessPermission("newpickle"));
    }

    /** Shortcut to DynamicEnum.getMembers */
    public static Set<PickleType> getMembers() {
        return getMembers(PickleType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static PickleType getMember(UUID u_id) throws NoSuchThingException {
        return getMember(u_id, PickleType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static PickleType getMember(String s_name) throws NoSuchThingException {
        return getMember(s_name, PickleType.class);
    }

    /**
     * Create a PickleMaker that can handle assets of the
     * given Asset-type.  In general, normal clients should
     * request AssetType.UNKNOWN or AssetType.GENERIC, then
     * let the GENERIC handler call back here to chain off
     * to the appropriate specialized asset type.
     *
     * @param n_asset to handle
     * @exception PickleClassException if no handler is
     *      registered for the given asset-type for littleware.asset.Asset
     */
    public abstract PickleMaker<Asset> createPickleMaker(AssetType<? extends Asset> n_asset)
            throws PickleClassException;

    /**
     * Shortcut to createPickleMaker( AssetType.UNKNOWN ) -
     * which most non-PickleMaker clients should use.
     * The AssetType.UNKNOWN handler should have logic in it
     * to call back to createPickleMaker( AssetType ) to chain
     * off pickle handling once it determines the actual pickle Asset-type.
     */
    public PickleMaker<Asset> createPickleMaker() {
        try {
            return createPickleMaker(AssetType.UNKNOWN);
        } catch (PickleClassException e) {
            throw new AssertionFailedException("No XML PickleMaker registered for AssetType.UNKNOWN", e);
        }
    }

    /**
     * Return true if hasPickleMaker( n_asset ) will succeed
     */
    public boolean hasPickleMaker(AssetType<? extends Asset> n_asset) {
        try {
            PickleMaker<Asset> pickle_maker = createPickleMaker(n_asset);
            return (pickle_maker != null);
        } catch (PickleClassException e) {
            return false;
        }
    }
    /**
     * Properties mapping AssetTypes to XML PickleMaker classes
     */
    private static Properties oprop_xml = null;
    

    static {
        Properties prop_defaults = new Properties();
        prop_defaults.setProperty(AssetType.UNKNOWN.getName(),
                littleware.asset.pickle.PickleXml.class.getName());
        prop_defaults.setProperty(AssetType.GENERIC.getName(),
                littleware.asset.pickle.PickleXml.class.getName());
        /*...
        prop_defaults.setProperty ( SecurityAssetType.QUOTA.getName (),
        littleware.web.pickle.xml.PickleQuota.class.getName ()
        );
        
        try {
        oprop_xml = PropertiesLoader.get().loadProperties ( "littleware_xml_pickle.properties" );
        } catch ( IOException e ) {
        olog_generic.log ( Level.WARNING, "Assuming default XML PickleMaker properties, caught unexpected: " + e + ", " + 
        BaseException.getStackTrace ( e ) 
        );
        oprop_xml = prop_defaults;
        }
         * */
        // Just hard code for now
        oprop_xml = prop_defaults;
    }
    public static final PickleType XML =
            new PickleType(UUIDFactory.parseUUID("388054B5354A43BCB5C06FDFF4E85DB4"),
            "littleware.XML_PICKLE_MAKER") {

        @Override
                public AssetXmlPickler createPickleMaker(AssetType<? extends Asset> n_asset) throws PickleClassException {
                    String s_classname = oprop_xml.getProperty(n_asset.getName());

                    if (null == s_classname) {
                        throw new PickleClassException("No pickle maker registered for asset of type: " +
                                n_asset.getName());
                    }

                    try {
                        Class<?> class_maker = Class.forName(s_classname);
                        return (AssetXmlPickler) class_maker.newInstance();
                    } catch (ClassNotFoundException e) {
                        throw new PickleClassException("Unable to load pickler: " + s_classname +
                                " for asset type: " + n_asset, e);
                    } catch (Throwable e) {
                        throw new PickleClassException("Unable to instantiate pickler: " + s_classname +
                                " for asset type: " + n_asset, e);
                    }
                }
            };
}

