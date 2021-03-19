package littleware.cloudutil

import com.google.inject

import java.net.URI
import java.util.UUID

import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ dnsValidator, notNullValidator, pathLikeValidator, rxValidator }

/**
 * Little resource name URI:
 * ```
 * "lrn://${cloud}/${api}/${project}/${resourceType}/${path}?tag1=${userTag1}&tag2=${userTag2}"
 * ```
 */
trait LRN {
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
case class LRPath(
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
case class LRId(
    cloud: String,
    api: String,
    projectId: UUID,
    resourceType: String,
    resourceId: UUID
) extends LRN {
    override val drawer = ":"
    val path = resourceId.toString()
}

object LRN {
    val zeroId:UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    trait Builder[T <: LRN] extends PropertyBuilder[T] {
        val cloud = new Property("") withName "cloud" withValidator dnsValidator
        val api = new Property("") withName "api" withValidator LRN.apiValidator
        val projectId = new Property[UUID](null) withName "projectId" withValidator notNullValidator
        val resourceType = new Property("") withName "resourceType" withValidator LRN.resourceTypeValidator
        val path = new Property("") withName "path" withValidator pathValidator

        def copy(lrn:T):this.type = this.projectId(lrn.projectId).api(lrn.api
              ).cloud(lrn.cloud).resourceType(lrn.resourceType
              ).path(lrn.path)

        def fromSession(session:Session): this.type = this.cloud(session.lrp.cloud
            ).api(session.api
            ).projectId(session.projectId
            )
    }

    class LRPathBuilder extends Builder[LRPath] {        
        val drawer = new Property("") withName "drawer" withValidator drawerValidator

        override def copy(other:LRPath) = super.copy(other).drawer(other.drawer)

        def build():LRPath = {
            validate()
            LRPath(cloud(), api(), projectId(), resourceType(), drawer(), path())
        }
    }

    class LRIdBuilder extends Builder[LRId] {
        def build():LRId = {
            validate()
            LRId(cloud(), api(), projectId(), resourceType(), UUID.fromString(path()))
        }
    }

    def apiValidator = rxValidator(raw"[a-z][a-z0-9-]+".r)(_, _)
    def drawerValidator(value:String, name:String) = rxValidator(raw"([\w-_.*]+:)*[\w-_.*]+".r)(value, name) orElse {
        if (value.length > 1000) {
            Some(s"${name} is too long: ${value}")
        } else {
            None
        }
    }
    def pathValidator(value:String, name:String) = pathLikeValidator(value, name) orElse {
        if (value.length > 1000) {
            Some(s"${name} is too long: ${value}")
        } else {
            None
        }
    }
    def resourceTypeValidator = rxValidator(raw"[a-z][a-z0-9-]+".r)(_, _)

    def lrnToURI(lrn:LRN):URI = {
        val (scheme, path) = lrn match {
            case lrpath:LRPath => "lrp" -> lrpath.path
            case lrid:LRId => "lrid" -> lrid.resourceId.toString()
        }
        new URI(s"${scheme}://${lrn.cloud}/${lrn.api}/${lrn.projectId}/${lrn.resourceType}/${lrn.drawer}/${path}")
    }

    def uriToLRN(uri:URI):LRN = {
        val builder = uri.getScheme() match {
            case "lrp" => new LRPathBuilder()
            case "lri" => new LRIdBuilder()
        }
        builder.cloud(uri.getHost())

        val pathRx = raw"/([^/]+)/([^/]+)/([^/]+)/(\S+)".r

        uri.getPath() match {
            case pathRx(api, projectId, resourceType, path) => {
                builder.api(api
                ).projectId(littleware.base.UUIDFactory.parseUUID(projectId)
                ).resourceType(resourceType
                ).path(path
                ).build()
            }
        }
    }
}
