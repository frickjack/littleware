package littleware.authzmgr

import java.net.{ URI, URL }
import java.time
import java.util.UUID
import scala.util._

import com.google.inject
import littleware.cloudutil.{ LittleResource, LRN, LRPath }
import littleware.scala.PropertyBuilder
import PropertyBuilder._

/**
 * Programmatic access at the project level
 */
case class Robot (
    id: UUID,
    updateTime: Long,
    state: String,
    lastUpdater: String,
    lrp: LRPath
) extends LittleResource {}

object Robot {
    val api = AuthzMgr.api
    val resourceType = "robot"

    class Builder @inject.Inject() (@inject.name.Named("little.cloudmgr.domain") defaultCloud: String) extends LittleResource.Builder[Robot](defaultCloud, Robot.api, Robot.resourceType) {
        def build():Robot = {
            this.validate()
            Robot(
                id(),
                updateTime(),
                state(),
                lastUpdater(),
                lrp() 
            )
        }
    }
}
