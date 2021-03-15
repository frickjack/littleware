package littleware.cloudutil

import com.google.inject

import java.net.URI
import java.util.UUID

import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ notNullValidator, rxValidator }

/**
 * Little resource name URI:
 * ```
 * "lrn://${cloud}/${api}/${project}/${resourceType}/${resourcePath}?tag1=${userTag1}&tag2=${userTag2}"
 * ```
 */
case class LRN (
    cloud: String,
    api: String,
    projectId: UUID,
    resourceType: String,
    resourcePath: String
    // maybe add this later: tags: Set[(String,String)]
) {}

object LRN {
    val zeroId:UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    class Builder @inject.Inject() (@inject.name.Named("little.cloud.domain") defaultCloud: String) extends PropertyBuilder[LRN] {
        val cloud = new Property(defaultCloud) withName "cloud" withValidator LRN.cloudValidator
        val api = new Property("") withName "api" withValidator LRN.apiValidator
        val projectId = new Property[UUID](null) withName "projectId" withValidator notNullValidator
        val resourceType = new Property("") withName "resourceType" withValidator LRN.resourceTypeValidator
        val resourcePath = new Property("") withName "resourcePath" withValidator LRN.resourcePathValidator
        
        def copy(lrn:LRN):this.type = this.projectId(lrn.projectId).api(lrn.api
              ).cloud(lrn.cloud).resourceType(lrn.resourceType
              ).resourcePath(lrn.resourcePath)

        def build():LRN = {
            validate()
            LRN(cloud(), api(), projectId(), resourceType(), resourcePath())
        }
    }

    def cloudValidator = rxValidator(raw"[a-z][0-9a-z]+".r)(_, _)
    def apiValidator = rxValidator(raw"[a-z][a-z0-9-]+".r)(_, _)
    def resourceTypeValidator = rxValidator(raw"[a-z][a-z0-9-]+".r)(_, _)
    def resourcePathValidator = rxValidator(raw"([\w-:_.*]+/)*[\w-:_.*]+".r)(_, _)
    def subjectValidator = rxValidator(raw"[a-z][a-z0-9_+-@.]+".r)(_, _)

    def lrnToURI(lrn:LRN):URI = new URI(s"lrn://${lrn.cloud}/${lrn.api}/${lrn.projectId}/${lrn.resourceType}/${lrn.resourcePath}")

    def uriToLRN(uri:URI):LRN = {
        if (uri.getScheme() != "lrn") {
            throw new IllegalArgumentException("not an lrn uri: " + uri)
        }
        val builder = new Builder("").cloud(uri.getHost())
        val pathRx = raw"/([^/]+)/([^/]+)/([^/]+)/(\S+)".r

        uri.getPath() match {
            case pathRx(api, projectId, resourceType, resourcePath) => {
                builder.api(api
                ).projectId(littleware.base.UUIDFactory.parseUUID(projectId)
                ).resourceType(resourceType
                ).resourcePath(resourcePath
                ).build()
            }
        }
    }
}
