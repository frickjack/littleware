package littleware.cloudutil

import java.util.UUID

import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ notNullValidator, positiveLongValidator }


trait LittleResource extends littleware.base.cache.CacheableObject {
    val id:java.util.UUID
    val updateTime:Long
    val lrp:LRPath

    override def getTimestamp() = updateTime
    override def getId() = id
    override def hashCode() = id.hashCode()
}

abstract class AbstractLittleResource

object LittleResource {
    /**
     * @param expectedApi provided by subtypes, and verified by validate
     * @param expectedResourceType provided by subtypes, and verified by validate
     */
    abstract class Builder[T <: LittleResource](expectedApi:String, expectedResourceType:String) extends PropertyBuilder[T] {
        def lrpValidator(lrp:LRPath, name:String):Option[String] = if (null == lrp) {
                Option(s"${name} is null")
            } else if (lrp.api != expectedApi) {
                Option(s"${name} api ${lrp.api} != ${expectedApi}")
            } else if (lrp.resourceType != expectedResourceType) {
                Option(s"${name} resourceType ${lrp.resourceType} != ${expectedResourceType}")
            } else {
                None
            }

        /**
         * lrpBuilder helper initialized with expectedApi and expectedResourceType
         */
        val lrpBuilder = new LRN.LRPathBuilder().api(expectedApi).resourceType(expectedResourceType)
        val lrp = new Property[LRPath](null) withName "lrp" withValidator lrpValidator
        val id = new Property(UUID.randomUUID()) withName "id" withValidator notNullValidator
        val updateTime = new Property(java.time.Instant.now().toEpochMilli()) withName "updateTime" withValidator positiveLongValidator


        /**
         * Call the given lambda with this as an argument,
         * and return this
         */
        def call(lambda:(this.type) => Unit):this.type = {
            lambda(this)
            this
        }


        /**
         * Shortcut for:
         * 
         *    call(
         *      { builder => builder.lrpBuilder.cloud(other.cloud).projectId(other.projectId) }
         *    )
         */
        def copyProject(other:LRN):this.type = this.call(
            { builder => builder.lrpBuilder.cloud(other.cloud).projectId(other.projectId) }
        )

        /**
         * Shortcut for:
         * 
         *    copyProject(other.lrp).lrp(other.lrp).id(other.id).updateTime(other.updateTime)
         */
        override def copy(other:T):this.type = copyProject(other.lrp).lrp(other.lrp).id(other.id).updateTime(other.updateTime)
    }

}
