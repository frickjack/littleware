/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.test;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import java.util.UUID;
import java.util.logging.Level;
import littleware.web.servlet.login.model.SessionInfo;
import littleware.web.servlet.login.controller.SessionMgr;

/**
 * Test the login session manager
 */
public class LoginTester extends littleware.test.LittleTest {
  {
    setName( "testSessionMgr" );
  }
  
  private final SessionMgr sessionMgr;
  
  @Inject()
  public LoginTester( SessionMgr sessionMgr ) {
    this.sessionMgr = sessionMgr;
  }
  
  
  public void testSessionMgr() {
    try {
      final UUID testId = UUID.randomUUID();
      final SessionInfo s1 = sessionMgr.loadSession(testId);
      assertTrue( "New session unauthenticated", s1.getActiveUser().isEmpty() );
      assertTrue( "Session reload hits cache", s1 == sessionMgr.loadSession( testId ) );
      sessionMgr.login( s1, "littleware.test_user", "test123");
      assertTrue( "Authenticated session looks ok", s1.getActiveUser().isSet() );
      
      final JsonObject js1 = sessionMgr.toJson( s1.getCredentials() );
      log.log( Level.INFO, "Credentials json: " + js1 );
      assertTrue( "To/from json looks consistent", 
              sessionMgr.fromJson( js1 ).equals( s1.getCredentials() )
              );
    } catch ( Exception ex ) {
      handle(ex);
    }
  }
  
}
