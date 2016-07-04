/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.UUID;
import littleware.asset.validate.AssetNameValidator;
import littleware.base.validate.AbstractProperty;
import littleware.base.validate.AbstractValidator;


/**
 * Asset summary info returned by "get*Ids" search function with name,
 * id, and time-stamp of assets.  The time-stamp is valuable for client-side cache
 * implementation, name for progressive UI, and id to fetch complete asset.
 */
public class AssetInfo implements java.io.Serializable {
    private UUID id;
    private String name;
    private long timestamp;

    private AssetInfo(UUID id, String name, long timestamp) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
    }

    /** Empty constructor for serializable */
    private AssetInfo() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Convenience method to extract the set of ids in the given collection of AssetInfo
     * @param infos
     * @return 
     */
    public static ImmutableSet<UUID> mapIds( Collection<AssetInfo> infos ) {
        final ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
        infos.stream().forEach( info -> builder.add( info.getId() ) );
        return builder.build();
    }
    
    public static class Builder extends AbstractValidator {
        private final AssetNameValidator nameValidator;
        
        @Inject
        public Builder( AssetNameValidator nameValidator ) {
            this.nameValidator = nameValidator;
        }
        
        public class Property<V> extends AbstractProperty<Builder,V> {
            public Property( String name, V value ) {
                super( Builder.this, name, value );
            }
        }
        
        public final Property<UUID> id = new Property<>( "id", null );
        
        public final Property<String> name = new Property<String>( "name", null ) {
          @Override
          public Collection<String> checkIfValid() {
              return this.buildErrorTracker().check( nameValidator.build( this.get() ) ).getErrors();
          }
        };
        
        public final Property<Long> timestamp = new Property<Long>( "timestamp", -1L );
        
        /**
         * Copy the id, name, and timestamp properties from the given source asset
         * 
         * @param source to copy properties from
         * @return this
         */
        public Builder copyFromAsset( Asset source ) {
            return id.set( source.getId() ).name.set( source.getName() ).timestamp.set( source.getTimestamp() );
        }

        @Override
        public Collection<String> checkIfValid() {
            return buildErrorTracker().check( id, name, timestamp ).getErrors();
        }
        
        
        public AssetInfo build() {
            this.validate();
            return new AssetInfo( id.get(), name.get(), timestamp.get() );
        }
    }
}

