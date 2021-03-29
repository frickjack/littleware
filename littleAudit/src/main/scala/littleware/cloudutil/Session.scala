package littleware.cloudutil

import java.net.{ URI, URL }
import java.time
import java.util.UUID
import scala.util._

import com.google.inject
import littleware.cloudutil.{ LittleResource, LRN }
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ emailValidator, notNullValidator, positiveLongValidator }


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
    state: String,
    lrp: LRPath
) extends LittleResource {
    override val updateTime = this.iat
    override val lastUpdater = this.subject
}

object Session {
    val api = "little-api"
    val resourceType = "session"

    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud: String) extends LittleResource.Builder[Session](defaultCloud, Session.api, Session.resourceType) {
        val cellId = new Property[UUID](null) withName "cellId" withValidator notNullValidator
        val subject = new Property("") withName "subject" withValidator emailValidator
        val projectId = new Property[UUID](null) withName "projectId" withValidator notNullValidator
        val api = new Property("") withName "api" withValidator LRN.apiValidator
        val iat = new OptionProperty[Long]() withName "exp" withMemberValidator positiveLongValidator
        val exp = new OptionProperty[Long]() withName "exp" withMemberValidator positiveLongValidator
        val endpoint = new OptionProperty[URL]() withName "endpoint" withMemberValidator notNullValidator
        val isAdmin = new Property(false) withName "isAdmin"
        val authClient = new Property[String](s"sessions@${defaultCloud}") withName "authClient" withValidator emailValidator

        override def copy(v:Session): this.type = super.copy(v
            ).cellId(v.cellId).subject(v.subject
            ).projectId(v.projectId
            ).api(v.api).exp.set(v.exp
            ).endpoint.set(v.endpoint
            ).isAdmin(v.isAdmin
            ).authClient(v.authClient)

        def build():Session = {
            val now = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"))
            lrp(
                lrpBuilder.projectId(LRN.zeroId).drawer(s"${projectId()}:${now.getYear()}").path(s"${now.getMonth()}/${subject()}/${id()}").build()
            )
            lastUpdater(subject())
            this.validate()
            val iatSecs = iat() getOrElse java.time.Instant.now().getEpochSecond()
            Session(
                id(), subject(), api(), projectId(), cellId(),
                endpoint() getOrElse new java.net.URL(s"https://${cellId()}.cells.${lrp().cloud}"),
                authClient(),
                isAdmin(),
                iatSecs,
                exp() getOrElse (iatSecs + 3600),
                state(),
                lrp()
            )
        }
    }
}
