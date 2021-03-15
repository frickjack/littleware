package littleware.cloudmgr

import com.google.inject
import java.net.{ URI, URL }
import java.util.UUID
import scala.util._

import littleware.cloudutil.{ LittleResource, LRN }
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ notNullValidator, positiveLongValidator, rxValidator }


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
    cloud: String,
    name: String,
    projectIds: Set[UUID],
    endpoint: URL,
    state: CellState,
    updateTime: Long,
    lrn: java.net.URI
) extends LittleResource {}

object Cell {
    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud: String) extends PropertyBuilder[Cell] {
        val cloud = new Property(defaultCloud) withName "cloud" withValidator LRN.cloudValidator
        val id = new OptionProperty[UUID]() withName "id"
        val name = new Property("") withName "name" withValidator rxValidator(raw"[a-z][a-z0-9_-+]+".r)
        val projectIds = new BufferProperty[UUID]() withName "projectIds"
        val state = new Property[CellState](CellState.Active) withName "state" withValidator notNullValidator
        val updateTime = new Property(java.time.Instant.now().getEpochSecond()) withName "updateTime" withValidator positiveLongValidator

        def copy(v:Cell): this.type = throw new UnsupportedOperationException("not yet implemented")
        def build():Cell = {
            this.validate()
            val lrn = new LRN.Builder(defaultCloud
                ).cloud(cloud()
                ).api("little-cloudmgr"
                ).resourceType("cell"
                ).resourcePath(id().toString()
                ).projectId(LRN.zeroId
                ).build()

            Cell(
                this.id() getOrElse UUID.randomUUID(),
                this.cloud(),
                this.name(),
                Set() ++ this.projectIds(),
                new java.net.URL(s"https://${ApiMgr.api}.${name()}.${cloud()}"),
                this.state(),
                updateTime(),
                LRN.lrnToURI(lrn)
            )
        }
    }
}
