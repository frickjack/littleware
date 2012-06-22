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
import org.eclipse.jetty.{servlet => jservlet}
import org.osgi
import scala.collection.JavaConversions._


/**
 * Bootstrap module registers message servlets with Jetty runtime.
 * The module requires that the littleware.asset JettyModule is booted up too.
 */
class JettyModuleFactory extends AppModuleFactory {
  override def build( profile:AppBootstrap.AppProfile ):AppModule = new JettyModuleFactory.JettyModule( profile )
}


object JettyModuleFactory {

  /** 
   * Bundle activator registers at startup registers
   * the message client servlet with the Jetty runtime
   */
  class Activator @inject.Inject()( 
    servletHandler:jservlet.ServletContextHandler,
    clientServlet:web.servlet.MessageServlet
  ) extends osgi.framework.BundleActivator {
    // register listener for test-messages
    servletHandler.addServlet( new jservlet.ServletHolder(clientServlet), "/services/message/*" )
    
    override def start( bc:osgi.framework.BundleContext ):Unit = {
    }
    
    override def stop( bc:osgi.framework.BundleContext ):Unit = {}
  }
  
  //-------------------------------------------

   
  class JettyModule ( profile:AppBootstrap.AppProfile ) extends helper.AbstractAppModule( profile ) {
    
    override def configure( binder:inject.Binder ):Unit = {
    }
    
    override def getActivator = classOf[Activator]
  }
   
  
}

