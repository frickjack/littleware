package littleware.cloudutil

import com.google.{ gson, inject }

import scala.jdk.CollectionConverters._


object ConfigHelper {
    val gs = new gson.GsonBuilder().setPrettyPrinting().create()
    
    /**
     * Parse the given json object, and return a map
     * from key to json string representation of value,
     * which can then be bound to @Named strings for
     * dependency injection (see bindConstant().annotatedWith(...))
     */
    def loadJsonMap(jsonStr:String): Map[String, String] = {
        gs.fromJson(jsonStr, classOf[gson.JsonObject]).entrySet().asScala.map( 
        entry => entry.getKey() -> gs.toJson(entry.getValue())
        ).toMap
    }

    /**
     * Little decorator for inject binder
     */
    class BindHelper(val binder: inject.Binder) {
        /**
        * Add a constant binding for key to value.
        * Return partially applied function, so can chain this if you want
        */
        def bindKeyValue(k:String, v:String):this.type = {
             binder.bindConstant().annotatedWith(inject.name.Names.named(k)).to(v)
             this
        }
    }
}