/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.s3Copy.model


import com.amazonaws.auth.AWSCredentials
import java.{io => jio}
import littleware.scala.PropertyBuilder


package config {

  case class S3Config private[model](
    val creds:AWSCredentials
    );


  object S3Config {
    class Builder () extends PropertyBuilder {
      val creds = new NotNullProperty[AWSCredentials]
      /** Little helper - loads creds from properties file on classpath */
      def credsFromResource( pathToProps:String ):this.type =
        throw new UnsupportedOperationException( "not yet implemented" )
      
      def credsFromFile( propsFile:jio.File ):this.type =
        throw new UnsupportedOperationException( "not yet implemented" )
      
      def build():S3Config = {
        val errors = checkSanity
        assert( errors.isEmpty, "Config Builder has errors: " + errors.mkString( "," ) )
        new S3Config( creds() )
      }
    }
  }

}