package littleware.scala

import com.google.gson
import com.google.inject

//import scala.jdk.CollectionConverters._

trait GsonProvider extends inject.Provider[gson.Gson] with java.util.function.Supplier[gson.Gson] {
  /**
   * Get an instance with pretty printing set true
   */
  def pretty():gson.Gson
}

/**
 * GsonProvider that gson consumers can use,
 * and custom type adapters can register with.
 */
object GsonProvider extends GsonProvider {
  private val gsBuilder = new gson.GsonBuilder()
  private val prettyBuilder = new gson.GsonBuilder().setPrettyPrinting()

  /**
   * prettyProvider returns pretty() for get() too
   */
  val prettyProvider = new GsonProvider() {
    override def get() = prettyBuilder.create()
    override def pretty() = get()
  }

  override def get() = gsBuilder.create()
  override def pretty() = prettyBuilder.create()

  /**
   * Prefer using the gson annotations to link a json type
   * adapter with a class over registering a type adapter here
   */
  def registerTypeAdapter​(typ:Class[_], typAdapter:Object): this.type = {
    Seq(gsBuilder, prettyBuilder).foreach({ builder => builder.registerTypeAdapter(typ, typAdapter) })
    this
  }

  /**
   * Goofy little helper to integrate Gson TypeAdapter
   * into scala collection framework.
   * Does a self-closing thing invoking reader.beginArray
   * and reader.endArray
   */
   def arrayIterator(reader:gson.stream.JsonReader): Iterator[gson.stream.JsonReader] = {
     reader.beginArray()
     new Iterator[gson.stream.JsonReader] {
       var isOpen = true
       override def hasNext = {
         if (isOpen) {
           isOpen = reader.hasNext
           if (!isOpen) { reader.endArray() }
         }
         isOpen
       }
       override def next() = reader
     }
   }


   def objectIterator(reader:gson.stream.JsonReader): Iterator[(String, gson.stream.JsonReader)] = {
     reader.beginObject()
     new Iterator[(String, gson.stream.JsonReader)] {
       var isOpen = true
       override def hasNext = {
         if (isOpen) {
           isOpen = reader.hasNext()
           if (!isOpen) { reader.endObject() }
         }
         isOpen
       }
       
       override def next() = (reader.nextName() -> reader)
     }
   }

}
