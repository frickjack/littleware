/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.message

import com.google.inject
import java.io
import java.sql
import littleware.bootstrap.{AppBootstrap,AppModule,AppModuleFactory,helper}
import org.osgi
import scala.collection.JavaConversions._

object LittleModuleFactory {

  /** 
   * Bundle activator registers at startup the default JAAS login configuration 
   * for use by the littleware server code, registers new bullingdon asset types
   * with registry
   */
  class Activator @inject.Inject()( 
  ) extends osgi.framework.BundleActivator {
    
    override def start( bc:osgi.framework.BundleContext ):Unit = {
    }
    
    override def stop( bc:osgi.framework.BundleContext ):Unit = {}
  }
  
  //-------------------------------------------

  private var _dataSource:Option[javax.sql.DataSource] = None
  
   
  class LittleAppModule ( profile:AppBootstrap.AppProfile ) extends helper.AbstractAppModule( profile ) {
    
    override def configure( binder:inject.Binder ):Unit = {
      binder.bind( classOf[model.Response.Builder] ).to( classOf[model.internal.ResponseBuilder])
      binder.bind( classOf[controller.MessageProcessor]
        ).to( classOf[controller.internal.SimpleMessageProcessor]
        ).in( inject.Scopes.SINGLETON
        )
      binder.bind( classOf[controller.MessageClient]
        ).toProvider( classOf[controller.internal.SimpleMessageProcessor.SMPClient.Provider]
        ).in( inject.Scopes.SINGLETON
        )
    }
    
    override def getActivator = classOf[Activator]
  }
   
  //-------------------------------------------
  
  /**
   * Littleware SessionModule establishes session-scoped bindings.
   * It's possible for an application to have multiple sessions (ex - a webapp)
   */
  class LittleSessionModule extends littleware.bootstrap.SessionModule {
    override def configure( binder:inject.Binder ):Unit = {
    }
    
    override def  getSessionStarter():Class[_ <: Runnable] = classOf[littleware.bootstrap.SessionModule.NullStarter]
  }
  
  //----------------
  
  /*
   * This thing runs at session startup time, and just adds a login listener to
   * littleware's login keychain.  
   *
   class SessionStarter @inject.Inject() ( keychain:KeyChain, loginListener:controller.internal.LoginListener ) extends Runnable {
   override def run():Unit = keychain.addPropertyChangeListener( loginListener )
   }
   */

}

/**
 * Littleware module data - combines both applications scope and
 * session scope binding module factories.
 */
class LittleModuleFactory extends AppModuleFactory with littleware.bootstrap.SessionModuleFactory {
  override def build( profile:AppBootstrap.AppProfile ):AppModule = new LittleModuleFactory.LittleAppModule( profile )
  override def buildSessionModule( profile:littleware.bootstrap.AppBootstrap.AppProfile ):littleware.bootstrap.SessionModule = new LittleModuleFactory.LittleSessionModule()  
}
