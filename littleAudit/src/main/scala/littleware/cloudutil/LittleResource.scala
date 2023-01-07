package littleware.cloudutil

import com.google.gson
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


object LittleResource {
    val defaultStateSet = Set("available", "building", "unavailable")
    /**
     * @param defaultCloud
     * @param expectedApi provided by subtypes, and verified by validate
     * @param expectedResourceType provided by subtypes, and verified by validate
     * @param defaultState default value for state property is "available"
     * @param stateSet set of valid states - defaults to defaultStateSet
     */
    abstract class Builder[T <: LittleResource](defaultCloud:String, expectedApi:String, expectedResourceType:String, defaultState:String, stateSet:Set[String]) extends PropertyBuilder[T] {
        def this(defaultCloud:String, expectedApi:String, expectedResourceType:String) = this(defaultCloud, expectedApi, expectedResourceType, "available", defaultStateSet)
        
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
         * lrpBuilder factory returns builder copied from current lrp() value if set,
         *   otherwise initialized with defaultCloud, expectedApi, and expectedResourceType
         */
        def lrpBuilder:LRN.LRPathBuilder = {
            val builder = new LRN.LRPathBuilder().cloud(defaultCloud).api(expectedApi).resourceType(expectedResourceType)
            Option(lrp()).foldLeft(builder){
                (builder, lrp) => builder.copy(lrp)
            }
        }

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
         * Shortcut for:
         * 
         *    copyProject(other.lrp).lrp(other.lrp).id(other.id
         *        ).updateTime(other.updateTime).state(other.state).lastUpdater()
         */
        override def copy(other:T):this.type = lrp(other.lrp).id(other.id
            ).updateTime(other.updateTime).state(other.state
            ).lastUpdater(other.lastUpdater)
    }

    /**
     * Little helper for subtypes building gson TypeAdapters
     */
    def gsonReader(builder:Builder[?], reader:gson.stream.JsonReader):PartialFunction[String,Unit] = _ match {
        case "lrp" => builder.lrp(LRN.uriToLRN(new java.net.URI(reader.nextString())).asInstanceOf[LRPath])
        case "id" => builder.id(UUID.fromString(reader.nextString()))
        case "updateTime" => builder.updateTime(reader.nextLong())
        case "state" => builder.state(reader.nextString())
        case "lastUpdater" => builder.lastUpdater(reader.nextString())
    }

    /**
     * Little json TypeAdapter writer helper - 
     * assumes caller handles beginObject/endObject
     */
    def gsonWriter(resource:LittleResource, writer:gson.stream.JsonWriter):gson.stream.JsonWriter =
        writer.name("lrp").value(LRN.lrnToURI(resource.lrp).toString()
            ).name("id").value(resource.id.toString()
            ).name("updateTime").value(resource.updateTime
            ).name("state").value(resource.state
            ).name("lastUpdater").value(resource.lastUpdater)
}
