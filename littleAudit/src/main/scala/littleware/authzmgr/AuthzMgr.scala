package littleware.authzmgr

import java.util.UUID

import littleware.cloudutil.RequestContext

trait PolicyDoc {}

trait Policy {}

trait AuthzMgr {
    def createPolicy(cx:RequestContext, doc:PolicyDoc, tags:Seq[(String,String)]):Policy
    def updatePolicy(cx:RequestContext, id:UUID, doc:PolicyDoc):Policy
    def updatePolicyTags(cx:RequestContext, id:UUID, tags:Seq[(String,String)]):Policy
}

object AuthzMgr {
    val api = "little-authz"
}