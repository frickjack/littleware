package littleware.cloudmgr

import java.net.{ URI, URL }
import java.time
import java.util.UUID
import scala.util._

import com.google.inject
import littleware.cloudutil.{ LittleResource, LRN }
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ notNullValidator, positiveLongValidator }


/**
 * Cell is a container for API's in a littleware cloud
 */
case class Session (
    id: UUID,
    subject: String,
    api: String,
    projectId: UUID,
    cellId: UUID,
    endpoint: URL,
    authClient: String,
    isAdmin: Boolean,
    iat: Long,
    exp: Long,
    lrn: java.net.URI
) extends LittleResource {
    override val updateTime = this.iat
}

object Session {
    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud: String) extends PropertyBuilder[Session] {
        val id = new Property(UUID.randomUUID()) withName "id" withValidator notNullValidator
        val cloud = new Property(defaultCloud) withName "cloud" withValidator LRN.cloudValidator
        val cellId = new Property[UUID](null) withName "cellId" withValidator notNullValidator
        val subject = new Property("") withName "subject" withValidator LRN.subjectValidator
        val projectId = new Property[UUID](null) withName "projectId" withValidator notNullValidator
        val api = new Property("") withName "api" withValidator LRN.apiValidator
        val iat = new Property(0L) withName "iat" withValidator positiveLongValidator
        val exp = new OptionProperty[Long]() withName "exp" withMemberValidator positiveLongValidator
        val endpoint = new OptionProperty[URL]() withName "endpoint" withMemberValidator notNullValidator
        val isAdmin = new Property(false) withName "isAdmin"
        val authClient = new Property[String](null) withName "authClient" withValidator notNullValidator

        def copy(v:Session): this.type = throw new UnsupportedOperationException("not yet implemented")
        def build():Session = {
            this.validate()
            Session(
                id(), subject(), api(), projectId(), cellId(),
                endpoint() getOrElse new java.net.URL(s"https://${cellId()}.${api()}.${cloud()}"),
                authClient(),
                isAdmin(),
                iat(), exp() getOrElse (iat() + 3600),
                LRN.lrnToURI(
                    new LRN.Builder(defaultCloud).api(api()).projectId(projectId()
                    ).resourceType("session").resourcePath(id().toString()
                    ).cloud(cloud()).build()
                )
            )
        }
    }
}
