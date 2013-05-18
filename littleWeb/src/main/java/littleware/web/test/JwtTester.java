/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import java.util.Map;
import littleware.base.Option;
import littleware.web.jwt.TokenFoundry;


/**
 * Test the littleware.web.jwt web token foundry
 */
public class JwtTester extends littleware.test.LittleTest {
  private final TokenFoundry foundry;
  
  @Inject()
  public JwtTester( TokenFoundry foundry ) {
    setName( "testJwtFoundry" );
    this.foundry = foundry;
  }
  
  public void testJwtFoundry() {
    try {
      final JsonObject jsIn = new JsonObject();
      jsIn.addProperty( "abc", "123" );
      jsIn.addProperty( "ABC", "456" );
      jsIn.addProperty( "xyz", "rgb" );
      
      final Option<JsonObject> optResult = foundry.verifyToken( foundry.makeToken( jsIn ));
      assertTrue( "Able to verify generated token", optResult.isSet() );
      final JsonObject jsOut = optResult.get();
      assertTrue( "JWT in/out preserves property count", jsIn.entrySet().size() == jsOut.entrySet().size() );
      for( Map.Entry<String,JsonElement> entry : jsIn.entrySet() ) {
        assertTrue( "Property values match: " + entry.getKey(), 
                entry.getValue().getAsString().equals( jsOut.get( entry.getKey() ).getAsString() )
                );
      }
    } catch ( Exception ex ) { 
      handle( ex ); 
    }  
  }
}
