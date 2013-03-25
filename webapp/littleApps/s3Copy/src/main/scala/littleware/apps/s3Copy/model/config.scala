/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.s3Copy.model


import com.amazonaws.auth
import java.{io => jio}
import littleware.scala.PropertyBuilder


package config {

  case class S3Config private[model](
    val creds:auth.AWSCredentials
    );


  object S3Config {
    class Builder () extends PropertyBuilder {
      val creds = new NotNullProperty[auth.AWSCredentials]
      
      /** Little helper - loads creds from properties file on classpath */
      def credsFromResource( pathToProps:String ):this.type = 
        credsFromProps( littleware.base.PropertiesLoader.get.loadProperties( pathToProps ) )
      
      def credsFromProps( props:java.util.Properties ):this.type = {
        require( props.containsKey( "accessKey" ) && props.containsKey( "secretKey" ), "S3 credentials file must specify 'accessKey' and 'secretKey'" )
        val basicCreds = new auth.BasicAWSCredentials( props.getProperty( "accessKey" ), props.getProperty( "secretKey" ))
        creds( basicCreds )        
      }
        

      
      def credsFromFile( propsFile:jio.File ):this.type = {
        val props = {
          val in = new java.io.FileInputStream( propsFile )
          require( null != in, "Able to read: " + propsFile )
          try {  val p =new java.util.Properties(); p.load( in ); p } finally in.close
        }
        credsFromProps( props )
      }
      
      def build():S3Config = {
        val errors = checkSanity
        assert( errors.isEmpty, "Config Builder has errors: " + errors.mkString( "," ) )
        new S3Config( creds() )
      }
    }
  }

}