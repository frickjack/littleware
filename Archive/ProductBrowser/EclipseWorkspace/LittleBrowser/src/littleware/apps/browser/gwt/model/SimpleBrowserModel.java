package littleware.apps.browser.gwt.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Simple browse model for initial browser implementation
 * that just navigates from one view to the next.
 */
public class SimpleBrowserModel implements java.io.Serializable {
	private final GwtOption<GwtAsset>   asset;
	private final Collection<GwtAsset>  children;
	private final Collection<GwtAsset>  siblings;
	private final Collection<GwtAsset>  parents;
    private final Map<GwtUUID,GwtAsset> index = new HashMap<GwtUUID,GwtAsset>();
    
	
	public SimpleBrowserModel(
			Collection<GwtAsset> parents,
			Collection<GwtAsset> siblings,
			Collection<GwtAsset> children,
			Collection<GwtAsset> linkedAssets,
			GwtAsset    assetIn
			) {
		this.asset = GwtOption.Factory.some( assetIn );
		this.parents = parents;
		this.siblings = siblings;
		this.children = children;
		final Collection<Collection<GwtAsset>> setOfSets = new ArrayList<Collection<GwtAsset>>();
		setOfSets.add( parents );
		setOfSets.add( siblings );
		setOfSets.add( children );
		setOfSets.add( linkedAssets );
		for( Collection<GwtAsset> assetSet : setOfSets 
		) {
			for( GwtAsset scan : assetSet ) {
				index.put( scan.getId(), scan );
			}
		}
		if ( this.asset.isEmpty() ) {
			if ( ! siblings.isEmpty() ) {
				throw new IllegalArgumentException( "Inconsistent asset, siblings setup" );
			}
		} else {
			for( GwtAsset scan : this.asset ) {
				for( GwtUUID parentId : GwtOption.Factory.some( scan.getFromId() ) ) {
					boolean foundAsset = false;
					for( GwtAsset sibling : this.siblings ) {
						if ( ! sibling.getFromId().equals( parentId ) ) {
							throw new IllegalArgumentException( "Inconsistent siblings" );
						}
						if ( sibling.getId().equals( scan.getId() ) ) {
							foundAsset = true;
						}
					}
					if ( ! foundAsset ) {
						throw new IllegalArgumentException( "Asset not in siblings set" );
					}
					boolean foundParent = false;
					for( GwtAsset parent : this.parents ) {
						if ( parent.getId().equals( parentId ) ) {
							foundParent = true;
							break;
						}
					}
					if ( ! foundParent ) {
						throw new IllegalArgumentException( "Asset parent not in parents set" );
					}
					for( GwtAsset child : this.children ) {
						if ( ! child.getFromId().equals( scan.getId() ) ) {
							throw new IllegalArgumentException( "Child does not have asset as parent" );
						}
					}
				}
			}
		}
	}
	
	/**
	 * Initialize empty model
	 */
	public SimpleBrowserModel() {
		this( new ArrayList<GwtAsset>(),
				new ArrayList<GwtAsset>(),
				new ArrayList<GwtAsset>(),
				new ArrayList<GwtAsset>(),
				null
		);
	}
	
	
	public Collection<GwtAsset> getParents() {
		return parents;
	}
	
	public Collection<GwtAsset> getSiblings() {
		return siblings;
	}
	
	public Collection<GwtAsset> getChildren() {
		return children;
	}
	
	public GwtOption<GwtAsset> resolve( GwtUUID id ) {
		return GwtOption.Factory.some( index.get(id) );
	}
	
	public GwtOption<GwtAsset> getAsset() {
		return asset;
	}
}
