package littleware.cloudmgr

import java.util.UUID
import scala.util._

import com.google.inject
import littleware.cloudutil.{ LittleResource, LRN, LRPath }
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ emailValidator, notNullValidator, positiveLongValidator, rxValidator }


/**
 * Project is a container for API's in a littleware cloud
 */
case class Project (
    id: UUID,
    owners: Set[String],
    client2Apis: Map[String,Set[String]],
    cellId: UUID,
    updateTime: Long,
    state: String,
    lastUpdater: String,
    lrp: LRPath
) extends LittleResource {}

object Project {
    val api = Cell.api
    val resourceType = "project"

    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud: String) extends LittleResource.Builder[Project](defaultCloud, Project.api, Project.resourceType) {
        val cellId = new Property[UUID](null) withName "cellId" withValidator notNullValidator
        val owners = new BufferProperty[String]() withName "owners" withMemberValidator emailValidator
        val client2Apis = new BufferProperty[(String, String)]() withName "client2Apis" withMemberValidator client2ApiValidator

        override def copy(v:Project):this.type = super.copy(v
            //).client2Apis.addAll(v.client2Apis.toSeq
            ).owners.addAll(v.owners
            ).cellId(v.cellId)

        def build():Project = {
            validate()

            Project(
                    id(),
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
                    updateTime(),
                    state(),
                    lastUpdater(),
                    lrp()
                )
        }
    }

    def client2ApiValidator(client2api:(String,String), name:String):Option[String] = client2api match { case (client -> api) => notNullValidator(client, name) orElse LRN.apiValidator(api, name) }
}
