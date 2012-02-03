/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provider;
import java.lang.reflect.Type;
import littleware.asset.AssetType;

/**
 * Builds a Gson instance suitable for serializing and deserializing littleware Assets
 * and serialization of supporting types (Date, UUID, AssetPath, ...)
 */
public interface LittleGsonFactory extends Provider<Gson> {
    /**
     * Get a GsonBuilder that uses the given resolver to resolve
     * sub-assets referenced from a JSON serialized asset in the
     * process of deserialization.  
     */
    public GsonBuilder getBuilder( LittleGsonResolver resolver );
    
    /**
     * Get a GsonBuilder with a NullGsonResolver - this instance will fail
     * to deserialize assets with references to subassets, but will otherwise
     * work fine for serialization and deserialization of standalone assets.
     */
    public GsonBuilder getBuilder();
    
    
    /** getBuilder( resolver ).create() */
    public Gson get( LittleGsonResolver resolver );
    
    /** getBuilder().create() */
    @Override
    public Gson get();
    
    /**
     * Register an extension to the Asset.class Gson TypeAdapter to
     * handle some particular AssetType ...
     * 
     * @return this so calls can be chained
     */
    public LittleGsonFactory registerAssetAdapter(GsonAssetAdapter adapter );
    
    /**
     * Register a custom type adapter to be passed to each new GsonBuilder and Gson object.
     * Note that LittleGsonFactory already registers internal adapters for UUID and Date.
     */
    public LittleGsonFactory registerTypeAdapter( Type clazz, Object adapter );
}
