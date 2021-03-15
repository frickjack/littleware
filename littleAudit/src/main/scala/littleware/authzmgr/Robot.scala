package littleware.authzmgr

import java.net.{ URI, URL }
import java.time
import java.util.UUID
import scala.util._

import com.google.inject
import littleware.cloudutil.{ LittleResource, LRN }
import littleware.scala.PropertyBuilder
import PropertyBuilder._

/**
 * Programmatic access at the project level
 */
case class Robot (
    id: UUID,
    name: String,
    projectId: UUID,
    updateTime: Long,
    lrn: java.net.URI
) extends LittleResource {}

object Robot {
    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud: String) extends PropertyBuilder[Robot] {
        val id = new Property(UUID.randomUUID()) withName "id" withValidator notNullValidator
        val cloud = new Property(defaultCloud) withName "cloud" withValidator LRN.cloudValidator
        val name = new Property("") withName "name" withValidator rxValidator(raw"[a-z][a-z0-9_+-.]+".r)
        val projectId = new Property[UUID](null) withName "projectId" withValidator notNullValidator
        val updateTime = new Property(0L) withName "updateTime" withValidator positiveLongValidator

        def copy(v:Robot): this.type = throw new UnsupportedOperationException("not yet implemented")

        def build():Robot = {
            this.validate()
            Robot(
                id(),
                name(),
                projectId(),
                updateTime(),
                LRN.lrnToURI(
                    new LRN.Builder(defaultCloud).api(AuthzMgr.api).projectId(projectId()
                    ).resourceType("robot").resourcePath(id().toString()
                    ).cloud(cloud()).build()
                ) 
            )
        }
    }
}
