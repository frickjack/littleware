/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.s3Copy

import com.amazonaws.services.s3
import com.google.inject
import java.io
import java.util.logging.{Level,Logger}
import littleware.bootstrap.{AppBootstrap,AppModule,AppModuleFactory,helper}
import org.osgi
import scala.collection.JavaConversions._
import scala.util.{Failure,Success,Try}


object LittleModuleFactory {
  private val log = Logger.getLogger( getClass.getName )
  
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
  
  /**
   * Try to initialize the S3 client with credentials
   * from awsCredentials.properties at session startup time ...
   */
  class SessionStarter @inject.Inject() (
    configMgr:controller.ConfigMgr,
    s3ConfigFactory:inject.Provider[model.config.S3Config.Builder],
    propLoader:littleware.base.PropertiesLoader
    ) extends Runnable {
      
    { 
      val optConfig:Option[model.config.S3Config] =
        Option( System.getProperty( "littleware.aws.configFile" )
             ).map( new java.io.File(_)
             ).filter( _.exists 
             ).flatMap( (propFile) => try {
                 Some( s3ConfigFactory.get.credsFromFile( propFile ).build )
               } catch {
                 case ex:Throwable => {
                     log.log( Level.WARNING, "Failed to load aws creds from: " + propFile, ex )
                     None
                 }
               }
             )
      if ( optConfig.isDefined ) {
        configMgr.s3Config( optConfig.get )
      } else {
        val resource = "aws/accessKeys.properties"
        try {
          configMgr.s3Config( s3ConfigFactory.get.credsFromResource( resource ).build )
        } catch {
          case ex:Throwable => {
              log.log( Level.WARNING, "Failed to load aws creds from default resource: " + resource )
          }
        }
      }
      
    }
    
    override def run() {}
  }
  
  //-------------------------------------------

  private var _dataSource:Option[javax.sql.DataSource] = None
  
   
  class LittleAppModule ( profile:AppBootstrap.AppProfile ) extends helper.AbstractAppModule( profile ) {
    
    override def configure( binder:inject.Binder ):Unit = {
      //binder.bind( classOf[model.Response.Builder] ).to( classOf[model.internal.ResponseBuilder])
    }
    
    override def getActivator = classOf[Activator]
  }
   
  //-------------------------------------------
  
  
  class S3Provider @inject.Inject() ( configMgr:controller.ConfigMgr ) 
    extends inject.Provider[controller.ConfigMgr.S3Factory] {
      override def get():controller.ConfigMgr.S3Factory = configMgr.s3Factory
  }
  
  /**
   * Littleware SessionModule establishes session-scoped bindings.
   * It's possible for an application to have multiple sessions (ex - a webapp)
   */
  class LittleSessionModule extends littleware.bootstrap.SessionModule {
    override def configure( binder:inject.Binder ):Unit = {
      //binder.bind( classOf[model.ClientSession] ).toProvider( classOf[model.internal.LittleSessionProvider] ).in( inject.Scopes.SINGLETON );
      binder.bind( classOf[controller.ConfigMgr] ).to( classOf[controller.internal.SimpleConfigMgr]
        ).in( inject.Scopes.SINGLETON )
      binder.bind( classOf[controller.PathTool] ).to( classOf[controller.internal.simplePathTool.SimpleTool]
        ).in( inject.Scopes.SINGLETON )
      
      binder.bind( classOf[s3.AmazonS3]).toProvider( classOf[controller.ConfigMgr.S3Factory] )
      binder.bind( classOf[controller.ConfigMgr.S3Factory] ).toProvider( classOf[S3Provider] )
      
    }
    
    override def  getSessionStarter():Class[_ <: Runnable] = classOf[SessionStarter]
  }
  

}

/**
 * Littleware module data - combines both applications scope and
 * session scope binding module factories.
 */
class LittleModuleFactory extends AppModuleFactory with littleware.bootstrap.SessionModuleFactory {
  override def build( profile:AppBootstrap.AppProfile ):AppModule = new LittleModuleFactory.LittleAppModule( profile )
  override def buildSessionModule( profile:littleware.bootstrap.AppBootstrap.AppProfile ):littleware.bootstrap.SessionModule = new LittleModuleFactory.LittleSessionModule()  
}
