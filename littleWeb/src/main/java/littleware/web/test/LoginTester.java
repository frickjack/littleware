/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.test;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import java.util.UUID;
import java.util.logging.Level;
import javax.servlet.http.Cookie;
import littleware.base.Option;
import littleware.base.Options;
import littleware.base.UUIDFactory;
import littleware.security.auth.LittleSession;
import littleware.web.servlet.login.model.SessionInfo;
import littleware.web.servlet.login.controller.SessionMgr;

/**
 * Test the login session manager
 */
public class LoginTester extends littleware.test.LittleTest {

    private final LittleSession loginSession;

    {
        setName("testSessionMgr");
    }

    private final SessionMgr sessionMgr;

    @Inject()
    public LoginTester(SessionMgr sessionMgr, LittleSession loginSession) {
        this.sessionMgr = sessionMgr;
        this.loginSession = loginSession;
    }

    public void testSessionMgr() {
        try {
            final UUID testId = UUID.randomUUID();
            final SessionInfo s1 = sessionMgr.loadSession(testId);
            assertTrue("New session unauthenticated", s1.getActiveUser().isEmpty());
            assertTrue("Session reload hits cache", s1 == sessionMgr.loadSession(testId));
            sessionMgr.login(s1, "littleware.test_user", "test123");
            assertTrue("Authenticated session looks ok", s1.getActiveUser().isSet());

            final JsonObject js1 = sessionMgr.toJson(s1.getCredentials());
            log.log(Level.INFO, "Credentials json: " + js1);
            assertTrue("To/from json looks consistent",
                    sessionMgr.fromJson(js1).equals(s1.getCredentials())
            );
            
            // test the authorizeRequest method a bit
            ImmutableList<Cookie> cookies = ImmutableList.of();
            final SessionMgr.AuthKey authKey1 = sessionMgr.authorizeRequest( cookies, Options.some( loginSession.getId().toString() ));
            assertTrue( "Header authorizes LoginFilter access", authKey1.authenticated && 
                    UUIDFactory.parseUUID( authKey1.info.getCredentials().getLoginCreds().get().authToken ).equals( loginSession.getId() )
            );
            assertTrue( "Header adds cookie", authKey1.addCookie );
            
            final Cookie cookie = new Cookie( SessionMgr.littleCookieName, sessionMgr.toJson( authKey1.info.getCredentials() ).toString() );
            final Option<String> empty = Options.empty();
            final SessionMgr.AuthKey authKey2 = sessionMgr.authorizeRequest( ImmutableList.of( cookie ), empty );
            assertTrue( "Cookie authorized ok", authKey2.authenticated &&  
                    UUIDFactory.parseUUID( authKey2.info.getCredentials().getLoginCreds().get().authToken ).equals( loginSession.getId() ) &&
                    (! authKey2.addCookie)
                    );
        } catch (Exception ex) {
            handle(ex);
        }
    }

}
