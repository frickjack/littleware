/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import littleware.bootstrap.AppBootstrap;

/**
 * Application scope singleton maintains cache of active sessions,
 * and manages creation of new sessions based on cookies or whatever.
 * When a new request comes in the following flow runs:
 * <ul>
 * <li> parse cookie
 * <li> lookup session
 * <li> login session if necessary
 * <li> save session to cache
 * </ul>
 */
public class SessionMgr {
  private final AppBootstrap boot;
  private final LoadingCache<UUID,SessionInfo> sessionCache =
    CacheBuilder.newBuilder().maximumSize( 1000 
      ).expireAfterWrite( 24, TimeUnit.HOURS 
      ).build(
          new CacheLoader<UUID,SessionInfo>() {
            @Override
           public SessionInfo load( UUID key ) {
             return boot.newSessionBuilder().sessionId( key ).build().startSession( SessionInfo.class );
           } 
          }
      );
  
  public SessionMgr( AppBootstrap boot ) {
    this.boot = boot;  
  }
  
  public SessionInfo loadSession( UUID id ) {
    return sessionCache.getIfPresent( id );
  }
}
