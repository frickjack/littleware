package littleware.apps.s3Copy

import com.amazonaws.services.s3
import com.google.inject
import java.io
import java.util.logging.{Level,Logger}
import littleware.bootstrap.{AppBootstrap,AppModule,AppModuleFactory,helper}
import scala.collection.JavaConversions._



package littleModule {
  object Config {
    val awsKeysResource:String = "aws/accessKeys.properties"
    private[littleModule] val log = Logger.getLogger( getClass.getName )  
  }
  
  import Config.log
  
  /**
   * Littleware module data - combines both applications scope and
   * session scope binding module factories.
   */
  class Factory extends AppModuleFactory with littleware.bootstrap.SessionModuleFactory {
    override def build( profile:AppBootstrap.AppProfile ):AppModule = new LittleAppModule( profile )
    override def buildSessionModule( profile:littleware.bootstrap.AppBootstrap.AppProfile ):littleware.bootstrap.SessionModule = new LittleSessionModule()  
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
      } else try {
          configMgr.s3Config( s3ConfigFactory.get.credsFromResource( Config.awsKeysResource ).build )
        } catch {
          case ex:Throwable => {
              log.log( Level.WARNING, "Failed to load aws creds from default resource: " + Config.awsKeysResource, ex )
          }
      }
      
      
    }
    
    override def run() {}
  }
  
  //-------------------------------------------
  
   
  class LittleAppModule ( profile:AppBootstrap.AppProfile ) extends helper.AbstractAppModule( profile ) {
    
    override def configure( binder:inject.Binder ):Unit = {
      //binder.bind( classOf[model.Response.Builder] ).to( classOf[model.internal.ResponseBuilder])
    }
    
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
      log.info( "Binding PathTool to simplePathTool.SimpleTool ..." );
      binder.bind( classOf[controller.PathTool] ).to( classOf[controller.internal.simplePathTool.SimpleTool]
        ).in( inject.Scopes.SINGLETON )
      
      binder.bind( classOf[s3.AmazonS3]).toProvider( classOf[controller.ConfigMgr.S3Factory] )
      binder.bind( classOf[controller.ConfigMgr.S3Factory] ).toProvider( classOf[S3Provider] )
      binder.bind( classOf[controller.UIEventHandler] ).to( classOf[controller.internal.SimpleEventHandler]).in( inject.Scopes.SINGLETON )
    }
    
    import java.util.Optional
    override def  getSessionStarter():Optional[Class[SessionStarter]] = Optional.of( classOf[SessionStarter] )
  }
  

}

