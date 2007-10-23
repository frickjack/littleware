package littleware.asset;

import java.util.UUID;

import littleware.base.Factory;
import littleware.base.FactoryException;
import littleware.base.UUIDFactory;

/**
 * Sort of a factory that will create an asset
 * of a given type, and initialize the asset's Properties (seters)
 * according to the set properties on the builder,
 * and also initializes the object-id with a UUID from
 * a UUIDFactory.
 * The AssetBuilder setters return this.
 */
public class AssetBuilder implements Factory<Asset> {
    private  UUID       ou_acl = null;
    private  UUID       ou_owner = null;
    private  AssetType  oatype_build = AssetType.GENERIC;
    private  String     os_comment = "";
    private  UUID       ou_home = null;
    private  UUID       ou_from = null;
    private  UUID       ou_to = null;
    private  String     os_name = null;
    
    private static final Factory<UUID> ofactory_uuid = UUIDFactory.getFactory ();
 
    /** Default to null */
    public AssetBuilder        setAclId ( UUID u_acl ) {
        ou_acl = u_acl;
        return this;
    }
    
    /** Default to null */
	public AssetBuilder        setOwnerId ( UUID u_owner ) {
        ou_owner = u_owner;
        return this;
    }
    
    /**
     * Set the asset-type on this builder - defaults to AssetType.GENERIC
     */
	public AssetBuilder        setAssetType ( AssetType<? extends Asset> atype_set ) {
        oatype_build = atype_set;
        return this;
    }

    /** Default to "" */    
	public AssetBuilder        setComment ( String s_comment ) {
        os_comment = s_comment;
        return this;
    }
    
    /** Default to null */    
	public AssetBuilder        setHomeId ( UUID u_home ) {
        ou_home = u_home;
        return this;
    }
    
    /** Default to null */    
	public AssetBuilder        setFromId ( UUID u_from ) {
        ou_from = u_from;
        return this;
    }
    
    /** Default to null */    
	public AssetBuilder        setName ( String s_name ) {
        os_name = s_name;
        return this;
    }

    
    /** Default to null */    
	public AssetBuilder        setToId ( UUID u_to ) {
        ou_to = u_to;
        return this;
    }
    
    public Asset create () throws FactoryException {
        Asset  a_new = oatype_build.create ();
        a_new.setObjectId ( ofactory_uuid.create () );
        a_new.setAclId ( ou_acl );
        a_new.setOwnerId ( ou_owner );
        a_new.setComment ( os_comment );
        a_new.setHomeId ( ou_home );
        a_new.setFromId ( ou_from );
        a_new.setToId ( ou_to );
        a_new.setName ( os_name );
        return a_new;
    }
    
    /** NOOP */
    public void recycle ( Asset a_bla ) {}
    
}
