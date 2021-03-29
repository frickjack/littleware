package littleware.cloudmgr

import com.google.inject
import java.net.{ URI, URL }
import java.util.UUID
import scala.util._

import littleware.cloudutil.{ LittleResource, LRN, LRPath }
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ dnsValidator, notNullValidator, positiveLongValidator, rxValidator }



/**
 * Cell is a container for API's in a littleware cloud
 */
case class Cell (
    id: UUID,
    endpoint: URL,
    updateTime: Long,
    state: String,
    lastUpdater: String,
    lrp: LRPath
) extends LittleResource {}

object Cell {
    val api = "little-api"
    val resourceType = "cell"

    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud: String) extends LittleResource.Builder[Cell](defaultCloud, Cell.api, Cell.resourceType) {
        override def lrpValidator(lrp:LRPath, name:String):Option[String] = 
            super.lrpValidator(lrp, name) match {
                case something:Some[String] => something
                case _ => dnsValidator(lrp.path, "lrp path dns check")
            }
        
        override def copy(v:Cell): this.type = super.copy(v)
        
        def build():Cell = {
            this.validate()

            Cell(
                id(),
                new java.net.URL(s"https://${id()}.cells.${lrp().cloud}"),
                updateTime(),
                state(),
                lastUpdater(),
                lrp()
            )
        }
    }
}
