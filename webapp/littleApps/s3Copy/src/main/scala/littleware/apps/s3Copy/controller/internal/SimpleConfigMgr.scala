package littleware.apps.s3Copy
package controller
package internal

import com.amazonaws.services.s3
import com.google.inject


class SimpleConfigMgr @inject.Inject() () extends ConfigMgr {
  private var _s3Config:Option[model.config.S3Config] = None
  private var _s3Client:Option[s3.AmazonS3] = None
  
  def   s3Config:Option[model.config.S3Config] = _s3Config
  def   s3Config( value:model.config.S3Config ):this.type = {
    _s3Config = Option(value)
    _s3Client = _s3Config.map( (config) => s3.AmazonS3ClientBuilder.standard().withCredentials(
        new com.amazonaws.auth.AWSStaticCredentialsProvider( config.creds )
      ).withRegion( com.amazonaws.regions.Regions.US_EAST_1 ).build() )
    this
  }
  
  /**
   * Factory supplies S3 clients via config
   */
  val   s3Factory = new ConfigMgr.S3Factory() {
    override def get():s3.AmazonS3 = _s3Client.getOrElse( { throw new IllegalStateException( "S3 client not yet configured" )})
  }

}


