package littleware.apps.s3Copy.model

import java.net.URI


/**
 * Folder with list of sub folders and objects in this folder
 */
case class FolderSummary private[model](
  path:URI,
  folders:Seq[java.net.URI],
  objects:Seq[ObjectSummary]
  ) extends PathSummary {}


object FolderSummary {
  class Builder () extends littleware.scala.PropertyBuilder {
    val path = new NotNullProperty[URI].name( "path" )
    val folders = new BufferProperty[java.net.URI]().name( "folders" )
    val objects = new BufferProperty[ObjectSummary]().name( "objects" )
    
    def build():FolderSummary = {
      val errors = this.checkSanity
      assert( errors.isEmpty, "Validation errs: " + errors.mkString( "," ) )
      FolderSummary( path(), folders().toSeq, objects().toSeq )
    }
  }
}