package littleware.cloudutil

import java.util.UUID

import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ emailValidator, notNullValidator, positiveLongValidator }


trait LittleResource extends littleware.base.cache.CacheableObject {
    val id:UUID
    val updateTime:Long
    val lrp:LRPath
    val state:String
    val lastUpdater:String

    override def getTimestamp() = updateTime
    override def getId() = id
    override def hashCode() = id.hashCode()
}

abstract class AbstractLittleResource

object LittleResource {
    val defaultStateSet = Set("available", "building", "unavailable")
    /**
     * @param expectedApi provided by subtypes, and verified by validate
     * @param expectedResourceType provided by subtypes, and verified by validate
     * @param defaultState default value for state property is "available"
     * @param stateSet set of valid states - defaults to defaultStateSet
     */
    abstract class Builder[T <: LittleResource](expectedApi:String, expectedResourceType:String, defaultState:String, stateSet:Set[String]) extends PropertyBuilder[T] {
        def this(expectedApi:String, expectedResourceType:String) = this(expectedApi, expectedResourceType, "available", defaultStateSet)
        
        def lrpValidator(lrp:LRPath, name:String):Option[String] = if (null == lrp) {
                Option(s"${name} is null")
            } else if (lrp.api != expectedApi) {
                Option(s"${name} api ${lrp.api} != ${expectedApi}")
            } else if (lrp.resourceType != expectedResourceType) {
                Option(s"${name} resourceType ${lrp.resourceType} != ${expectedResourceType}")
            } else {
                None
            }

        def stateValidator(value:String, name:String):Option[String] = {
            if (value != null && raw"[a-z0-9_-]{1,12}".r.matches(value) && stateSet.contains(value)) {
                None
            } else {
                Some(s"Invalid ${name}: ${value}")
            }
        }

        /**
         * lrpBuilder helper initialized with expectedApi and expectedResourceType
         */
        val lrpBuilder = new LRN.LRPathBuilder().api(expectedApi).resourceType(expectedResourceType)
        val lrp = new Property[LRPath](null) withName "lrp" withValidator lrpValidator
        val id = new Property(UUID.randomUUID()) withName "id" withValidator notNullValidator
        val updateTime = new Property(java.time.Instant.now().toEpochMilli()) withName "updateTime" withValidator positiveLongValidator
        val state = new Property(defaultState) withName "state" withValidator stateValidator
        val lastUpdater = new Property("") withName "lastUpdater" withValidator emailValidator

        /**
         * Call the given lambda with this as an argument,
         * and return this
         */
        def call(lambda:(this.type) => Unit):this.type = {
            lambda(this)
            this
        }


        /**
         * Copy project and updater info from given session.
         * Shortcut for:
         * 
         *    call(
         *      { builder => builder.lrpBuilder.cloud(other.cloud).projectId(other.projectId) }
         *    ).lastUpdater(session.subject)
         */
        def copySession(other:Session):this.type = this.call(
            { builder => builder.lrpBuilder.cloud(other.lrp.cloud).projectId(other.lrp.projectId) }
        ).lastUpdater(other.subject)

        /**
         * Shortcut for:
         * 
         *    copyProject(other.lrp).lrp(other.lrp).id(other.id
         *        ).updateTime(other.updateTime).state(other.state).lastUpdater()
         */
        override def copy(other:T):this.type = lrp(other.lrp).id(other.id
            ).updateTime(other.updateTime).state(other.state
            ).lastUpdater(other.lastUpdater)
    }

}
