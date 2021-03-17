package littleware.cloudmgr

import com.google.inject
import java.net.{ URI, URL }
import java.util.UUID
import scala.util._

import littleware.cloudutil.{ LittleResource, LRN, LRPath }
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ dnsValidator, notNullValidator, positiveLongValidator, rxValidator }


sealed trait CellState {}


object CellState {
    case object Active extends CellState {
        override def toString() = "active"
    }

    case object Inactive extends CellState {
        override def toString() = "inactive"
    }

    case object Updating extends CellState {
        override def toString() = "updating"
    }


    def fromString(value: String):Try[CellState] = Try(
        {
            value match {
                case "active" => CellState.Active
                case "inactive" => CellState.Inactive
                case "updating" => CellState.Updating
                case _ => throw new IllegalArgumentException("invalid state string: " + value)
            }
        }
    )
}

/**
 * Cell is a container for API's in a littleware cloud
 */
case class Cell (
    id: UUID,
    endpoint: URL,
    state: CellState,
    updateTime: Long,
    lrp: LRPath
) extends LittleResource {}

object Cell {
    val api = ApiMgr.api
    val resourceType = "cell"

    class Builder extends LittleResource.Builder[Cell](Cell.api, Cell.resourceType) {
        override def lrpValidator(lrp:LRPath, name:String):Option[String] = 
            super.lrpValidator(lrp, name) match {
                case something:Some[String] => something
                case _ => dnsValidator(lrp.path, "lrp path dns check")
            }

        val state = new Property[CellState](CellState.Active) withName "state" withValidator notNullValidator
        
        override def copy(v:Cell): this.type = super.copy(v).state(v.state)
        
        def build():Cell = {
            this.validate()

            Cell(
                this.id(),
                new java.net.URL(s"https://${id()}.cells.${lrp().cloud}"),
                this.state(),
                updateTime(),
                lrp()
            )
        }
    }
}
