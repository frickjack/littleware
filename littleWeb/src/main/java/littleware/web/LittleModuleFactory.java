/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import littleware.base.Option;
import littleware.base.Options;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.web.jwt.TokenFoundry;
import littleware.web.jwt.internal.SimpleTokenFoundry;
import littleware.web.servlet.login.LoginServlet;
import littleware.web.servlet.login.controller.SessionMgr;
import littleware.web.servlet.login.controller.internal.SimpleSessionMgr;
import littleware.web.servlet.login.model.SessionInfo;
import littleware.web.servlet.login.model.internal.SimpleSessionInfo;


/**
 * Littleware module factory registers and configures web classes
 * with Guice and startup/shutdown systems.
 */
public class LittleModuleFactory implements AppModuleFactory, SessionModuleFactory {

  public static class MyAppModule extends AbstractAppModule {

    public MyAppModule( AppProfile profile ){ super( profile ); }
    

    @Override
    public void configure(Binder binder) {
      binder.bind( SessionMgr.class ).to( SimpleSessionMgr.class ).in( Scopes.SINGLETON );
      binder.bind( SimpleSessionMgr.class ).in( Scopes.SINGLETON );
      binder.bind( TokenFoundry.class ).to( SimpleTokenFoundry.class ).in( Scopes.SINGLETON );
      binder.bind( SimpleTokenFoundry.class ).in( Scopes.SINGLETON );
    }
    
  }
  
  //--------------------------------------
  
  public static class MySessionModule implements SessionModule {

    @Override
    public Option<Class<Runnable>> getSessionStarter() {
      return Options.empty();
    }

    @Override
    public void configure(Binder binder) {
      binder.bind( LoginServlet.class ).in( Scopes.SINGLETON );
      binder.bind( SessionInfo.class ).to( SimpleSessionInfo.class ).in( Scopes.SINGLETON );
      binder.bind( SimpleSessionInfo.class ).in( Scopes.SINGLETON );
    }
    
  }

  //--------------------------------------
  
  @Override
  public AppModule build(AppProfile ap) {
    return new MyAppModule( ap );
  }

  @Override
  public SessionModule buildSessionModule(AppProfile ap) {
    return new MySessionModule();
  }
  
}
