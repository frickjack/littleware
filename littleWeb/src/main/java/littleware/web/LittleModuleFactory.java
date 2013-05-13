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
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import littleware.bootstrap.helper.NullActivator;
import org.osgi.framework.BundleActivator;

/**
 * Littleware module factory registers and configures web classes
 * with Guice and startup/shutdown systems.
 */
public class LittleModuleFactory implements AppModuleFactory, SessionModuleFactory {

  public static class MyAppModule implements AppModule {
    private final AppProfile profile;

    public MyAppModule( AppProfile profile ) { this.profile = profile; }
    
    @Override
    public AppProfile getProfile() {
      return profile;
    }

    @Override
    public Class<? extends BundleActivator> getActivator() {
      return NullActivator.class;
    }

    @Override
    public void configure(Binder binder) {
      binder.bind( littleware.web.servlet.login.SessionMgr.class ).in( Scopes.SINGLETON );
    }
    
  }
  
  //--------------------------------------
  
  public static class MySessionModule implements SessionModule {

    @Override
    public Class<? extends Runnable> getSessionStarter() {
      return NullStarter.class;
    }

    @Override
    public void configure(Binder binder) {
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