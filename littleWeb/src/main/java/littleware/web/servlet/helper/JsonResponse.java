/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.helper;

import com.google.gson.JsonObject;
import java.util.Collection;
import littleware.base.validate.AbstractProperty;
import littleware.base.validate.AbstractValidator;



/**
 * Little helper for assembling a JSON response in doGetOrPostOrPut
 */
public class JsonResponse {

  private final int status;
  private final JsonObject content;

  private JsonResponse(int status, JsonObject content) {
    this.status = status;
    this.content = content;
  }

  int getStatus() {
    return status;
  }

  JsonObject getContent() {
    return content;
  }
  
  public static class Builder extends AbstractValidator {
    
    public class Property<V> extends AbstractProperty<Builder,V> {
      public Property( String name, V value ) {
        super( Builder.this, name, value );
      }
    }
    
    public final Property<Integer> status = new Property<Integer>( "status", 200 ) {
      @Override
      public Collection<String> checkIfValid() {
        return buildErrorTracker().check( value != null && value > 99 && value < 600, "HTTP status code in valid range (100,500):" + this).getErrors();
      }
    };
            
    public final Property<JsonObject> content = new Property<JsonObject>( "content", new JsonObject() ) {
      @Override
      public Collection<String> checkIfValid() {
        return buildErrorTracker().check( value != null && ! value.entrySet().isEmpty(), "valid json: " + this ).getErrors();
      }
    };
    

    @Override
    public Collection<String> checkIfValid() {
      return buildErrorTracker().check( status, content ).getErrors();
    }

    @Override
    public String toString() {
      return new StringBuilder().append( status ).append( content ).toString();
    }

    /**
     * Builds JsonResponse - adds property:
     *    { "littleStatus" : status.get() }
     * to content as side effect.
     */
    public JsonResponse build() {
      this.content.get().addProperty( "littleStatus", this.status.get().intValue() );
      this.validate();
      return new JsonResponse( status.get(), content.get() );
    }
  }
}
