package littleware.apps.browser.gwt.model.internal;

import java.util.Collection;
import java.util.Collections;

import littleware.apps.browser.gwt.model.GwtEverybody;
import littleware.apps.browser.gwt.model.GwtGroup;
import littleware.apps.browser.gwt.model.GwtHome;
import littleware.apps.browser.gwt.model.GwtPrincipal;
import littleware.apps.browser.gwt.model.GwtUser;

public class SimpleEverybody extends SimpleGroupBuilder.SimpleGroup implements GwtEverybody {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isMember( GwtUser member ) { return false; }
	@Override
	public Collection<GwtPrincipal> getMembers() {
		throw new UnsupportedOperationException( "May not retrieve members of everybody group" );
	}
	
	private final static Collection<GwtPrincipal> empty = Collections.emptyList();
	
	public SimpleEverybody() {
		super(
				(SimpleGroupBuilder) GwtGroup.Factory.get().id( GwtEverybody.id 
						).parentId( GwtHome.littleHomeId 
						).homeId( GwtHome.littleHomeId 
					    ).build(),
				empty
				);
	}
}
