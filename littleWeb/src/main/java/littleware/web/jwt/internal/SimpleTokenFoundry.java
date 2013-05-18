/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.jwt.internal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.PropertiesLoader;
import littleware.base.Whatever;
import littleware.web.jwt.TokenFoundry;

@Singleton
public class SimpleTokenFoundry implements TokenFoundry {

  private static final Logger log = Logger.getLogger(SimpleTokenFoundry.class.getName());
  private final Gson gsonTool;

  @Inject
  public SimpleTokenFoundry(Gson gsonTool) {
    this.gsonTool = gsonTool;
  }

  private static String loadSecret() {
    try {
      return Maybe.something( PropertiesLoader.get().loadProperties(SimpleTokenFoundry.class).getProperty("sharedSecret") ).get();
    } catch (IOException ex) {
      log.log(Level.WARNING, "Failed to load JWT shatred secret", ex);
      throw new IllegalStateException("No shared secret configured");
    }
  }
  
  private final String secret = loadSecret();
  private final byte[] secretBytes = secret.getBytes(Whatever.UTF8);

  @Override
  public String makeToken(JsonObject js) {
    // Copied from: https://bitbucket.org/nimbusds/nimbus-jose-jwt/wiki/Home
    // Create JWS payload
    final Payload payload = new Payload(js.toString());

    // Create JWS header with HS256 algorithm
    final JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
    header.setContentType("application/json");

    // Create JWS object
    final JWSObject jwsObject = new JWSObject(header, payload);

    final JWSSigner signer = new MACSigner(secretBytes);
    try {
      jwsObject.sign(signer);
    } catch (JOSEException ex) {
      throw new IllegalStateException("Failed to sign web token", ex);
    }

    // Serialise JWS object to compact format
    return jwsObject.serialize();
  }

  @Override
  public Option<JsonObject> verifyToken(String token) {
// Parse back and check signature
    final JWSObject jwsObject;
    try {
      jwsObject = JWSObject.parse(token);
    } catch (ParseException ex) {
      log.log(Level.FINE, "Failed to parse given string as a jwt token: {0}", token);
      return Maybe.empty();
    }

    final JWSVerifier verifier = new MACVerifier(secretBytes);

    final boolean verifiedSignature;
    try {
      verifiedSignature = jwsObject.verify(verifier);
    } catch (JOSEException ex) {
      log.log(Level.FINE, "Signature verification threw exception", ex);
      return Maybe.empty();
    }

    if (!verifiedSignature) {
      return Maybe.empty();
    }

    return Maybe.something(gsonTool.fromJson(jwsObject.getPayload().toString(), JsonElement.class).getAsJsonObject());
  }
}
