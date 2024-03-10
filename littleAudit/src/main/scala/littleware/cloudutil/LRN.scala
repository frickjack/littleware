package littleware.cloudutil

import com.google.gson
import com.google.inject

import java.net.URI
import java.util.UUID

import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ dnsValidator, notNullValidator, pathLikeValidator, rxValidator }

/**
 * Little resource name URI:
 * "lrn://${cloud}/${api}/${project}/${resourceType}/${path}?tag1=${userTag1}&tag2=${userTag2}"
 *
 * the idea is to emulate AWS arn -
 *   https://docs.aws.amazon.com/IAM/latest/UserGuide/reference-arns.html
 *
 * @param drawer tags related resources for billing and security rules
 */
sealed trait LRN {
    val cloud: String
    val api: String
    val projectId: UUID
    val resourceType: String
    val drawer: String
    val path: String
    // maybe add this later: tags: Set[(String,String)]
}

/**
 * Path based sharing - denormalized data
 */
@gson.annotations.JsonAdapter(classOf[LRN.GsonTypeAdapter])
case class LRPath private[cloudutil] (
    cloud: String,
    api: String,
    projectId: UUID,
    resourceType: String,
    drawer: String,
    path: String
) extends LRN {
}

/**
 * Id based sharing - normalized data
 */
@gson.annotations.JsonAdapter(classOf[LRN.GsonTypeAdapter])
case class LRId private[cloudutil] (
    cloud: String,
    api: String,
    projectId: UUID,
    resourceType: String,
    resourceId: UUID
) extends LRN {
    override val drawer = "-"
    val path = resourceId.toString()
}

object LRN {
    val zeroId:UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    sealed trait Builder[T <: LRN] extends PropertyBuilder[T] {
        val cloud = new Property("") withName "cloud" withValidator dnsValidator
        val api = new Property("") withName "api" withValidator LRN.apiValidator
        val projectId = new Property[UUID](null) withName "projectId" withValidator notNullValidator
        val resourceType = new Property("") withName "resourceType" withValidator LRN.resourceTypeValidator
        val drawer = new Property("") withName "drawer" withValidator drawerValidator
        val path = new Property("") withName "path" withValidator pathValidator

        def copy(lrn:T):this.type = this.projectId(lrn.projectId).api(lrn.api
            ).cloud(lrn.cloud).resourceType(lrn.resourceType
            ).path(lrn.path)

        def fromSession(session:Session): this.type = this.cloud(session.lrp.cloud
            ).api(session.api
            ).projectId(session.projectId)
    }

    private[cloudutil] class LRPathBuilder extends Builder[LRPath] {
        override def copy(other:LRPath) = super.copy(other).drawer(other.drawer)

        def build():LRPath = {
            validate()
            LRPath(cloud(), api(), projectId(), resourceType(), drawer(), path())
        }
    }

    def pathBuilder():Builder[LRPath] = new LRPathBuilder()


    private[cloudutil] class LRIdBuilder extends Builder[LRId] {
        override val drawer = new Property("-") withName "drawer" withValidator rxValidator(raw"-".r)

        def build():LRId = {
            validate()
            LRId(cloud(), api(), projectId(), resourceType(), UUID.fromString(path()))
        }
    }

    def idBuilder():Builder[LRId] = new LRIdBuilder()

    def apiValidator = rxValidator(raw"[a-z][a-z0-9-]+".r)(_, _)
    def drawerValidator(value:String, name:String) = rxValidator(raw"[\w-_\.]+".r)(value, name) orElse {
        if (value.length > 1000) {
            Some(s"${name} is too long: ${value}")
        } else {
            None
        }
    }
    
    def pathValidator(value:String, name:String) = pathLikeValidator(value, name) orElse {
        Option.when(value.length > 1000)(s"${name} is too long: ${value}")
    }

    def resourceTypeValidator = rxValidator(raw"[a-z][a-z0-9-]{1,20}".r)(_, _)

    def lrnToURI(lrn:LRN):URI = {
        val scheme = lrn match {
            case lrpath:LRPath => "lrp"
            case lrid:LRId => "lrid"
        }
        new URI(s"${scheme}://${lrn.cloud}/${lrn.api}/${lrn.projectId}/${lrn.resourceType}/${lrn.drawer}/${lrn.path}")
    }

    def uriToLRN(uri:URI):LRN = {
        val builder = uri.getScheme() match {
            case "lrp" => new LRPathBuilder()
            case "lri" => new LRIdBuilder()
        }
        builder.cloud(uri.getHost())

        val pathRx = raw"/([^/]+)/([^/]+)/([^/]+)/([^/]+)/(\S+)".r

        uri.getPath() match {
            case pathRx(api, projectId, resourceType, drawer, path) => {
                builder.api(api
                ).projectId(littleware.base.UUIDFactory.parseUUID(projectId)
                ).resourceType(resourceType
                ).drawer(drawer
                ).path(path
                ).build()
            }
        }
    }
    

    class GsonTypeAdapter extends gson.TypeAdapter[LRN]() {
        override def read(reader:gson.stream.JsonReader):LRN = {
            uriToLRN(new URI(reader.nextString()))
        }
        
        override def write(writer:gson.stream.JsonWriter, src:LRN):Unit = {
            writer.value(lrnToURI(src).toString())
        }
    }

    val gsonTypeAdapter = new GsonTypeAdapter()

}
