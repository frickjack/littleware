package littleware.cloudmgr

import java.util.UUID
import scala.util._

import com.google.inject
import littleware.cloudutil.{ LittleResource, LRN }
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ notNullValidator, positiveLongValidator, rxValidator }

sealed trait ProjectState {}


object ProjectState {
    case object Active extends ProjectState {
        override def toString() = "active"
    }

    case object Inactive extends ProjectState {
        override def toString() = "inactive"
    }

    case object Updating extends ProjectState {
        override def toString() = "updating"
    }


    def fromString(value: String):Try[ProjectState] = Try(
        {
            value match {
                case "active" => ProjectState.Active
                case "inactive" => ProjectState.Inactive
                case "updating" => ProjectState.Updating
                case _ => throw new IllegalArgumentException("invalid state string: " + value)
            }
        }
    )
}

/**
 * Project is a container for API's in a littleware cloud
 */
case class Project (
    id: UUID,
    cloud: String,
    name: String,
    owners: Set[String],
    client2Apis: Map[String,Set[String]],
    cellId: UUID,
    state: ProjectState,
    updateTime: Long,
    lrn: java.net.URI
) extends LittleResource {}

object Project {
    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud:String, lrnFactory:inject.Provider[LRN.Builder]) extends PropertyBuilder[Project] {
        val cloud = new Property(defaultCloud) withName "cloud" withValidator LRN.cloudValidator
        val id = new OptionProperty[UUID]() withName "id"
        val cellId = new Property[UUID](null) withName "cellId" withValidator notNullValidator
        val name = new Property("") withName "name" withValidator rxValidator(raw"[a-z][a-z0-9_-+]+".r)
        val owners = new BufferProperty[String]() withName "owners" withMemberValidator LRN.subjectValidator
        val client2Apis = new BufferProperty[(String, String)]() withName "client2Apis" withMemberValidator client2ApiValidator
        val state = new Property[ProjectState](ProjectState.Active) withName "state" withValidator notNullValidator
        val updateTime = new Property(java.time.Instant.now().getEpochSecond()) withName "updateTime" withValidator positiveLongValidator

        def copy(v:Project):this.type = id.set(v.id
            //).client2Apis.addAll(v.client2Apis.toSeq
            ).owners.addAll(v.owners
            ).cellId(v.cellId
            ).state(v.state
            ).name(v.name
            ).cloud(v.cloud
            ).updateTime(v.updateTime)

        def build():Project = {
            validate()
            val lrn = lrnFactory.get(
                ).cloud(cloud()
                ).api("little-cloud"
                ).resourceType("project"
                ).resourcePath(id().toString()
                ).projectId(LRN.zeroId
                ).build()

            Project(
                    id() getOrElse UUID.randomUUID(),
                    cloud(),
                    name(),
                    Set() ++ this.owners(), 
                    client2Apis().toSet[(String,String)].groupMap(
                            _ match { 
                                case (client -> api) => client
                            }
                        )(
                            _ match {
                                case (client -> api) => api
                            }
                        ),
                    cellId(),
                    state(),
                    updateTime(),
                    LRN.lrnToURI(lrn)
                )
        }
    }

    def client2ApiValidator(client2api:(String,String), name:String):Option[String] = client2api match { case (client -> api) => notNullValidator(client, name) match { case None => LRN.apiValidator(api, name); case clientError => clientError }}
}
