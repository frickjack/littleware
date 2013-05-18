/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.jwt;


import com.google.gson.JsonObject;
import com.google.inject.Singleton;
import littleware.base.Option;
        
/**
 * Little helper for assembling web tokens
 */
public interface TokenFoundry {
  /**
   * Make a token using the default internal shared secret
   * using HMAC (hash based message authentication code) with
   * SHA256 hash.
   *    https://bitbucket.org/nimbusds/nimbus-jose-jwt/wiki/Home
   * 
   * @param js
   * @return token compacted as string
   */
  String makeToken( JsonObject js );  
  
  /**
   * Extract the JSON content from the given token iff the token
   * passes signature validation with the shared secret.
   * 
   * @param token
   * @return the extracted json object if token passes validation, otherwise empty
   */
  Option<JsonObject> verifyToken( String token );
}
