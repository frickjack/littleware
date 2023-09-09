package littleware.scala

import com.google.gson
import com.google.inject

import scala.jdk.CollectionConverters._


/**
 * Config loader.
 * <ul>
 * <li> retrieve LITTLE_CONFIG_PATH system property or environment variable,
 *      split on semi-colon (to support URI's in the future ...)
 * <li> user requests config-key like LITTLE_CLOUDMGR
 * <li> load the first LITTLE_CLOUDMGR.json file found searching
 *      each LITTLE_CONFIG_PATH entry
 * <li> load LITTLE_CLOUDMGR.json off the classpath
 * <li> load LITTLE_CLOUDMGR environment variable
 * <li> we assume that we load json objects, and start with
 *      classpath object, then shallow merge the search path
 *      files on top of that, then the environment on top of that:
 *      Object.assign(classPathValues, searchPathValues, envValues)
 * </ul>
 */
object JsonConfigLoader {
  private val gs = GsonProvider.get()
  val utf8 = "UTF-8"

  val LITTLE_CONFIG_PATH = "LITTLE_CONFIG_PATH"
  lazy val searchPath = Option(
    System.getProperty(LITTLE_CONFIG_PATH, System.getenv(LITTLE_CONFIG_PATH))
  ).toSeq.flatMap({ pathSpec => pathSpec.split(";") })

  /**
   * Load the json file if any at (key + ".json")
   * on the classpath
   */
  def loadClasspathConfig(key:String, classLoader:ClassLoader): Option[gson.JsonObject] = {
    Option(classLoader.getResourceAsStream(key + ".json")).map(
      {
        istream =>
          val reader = new java.io.InputStreamReader(istream, utf8)
          try {
            gs.fromJson(reader, classOf[gson.JsonObject])
          } finally {
            reader.close()
          }
      }
    )
  }

  /**
   * Load the first json file if any at (key.replaceAll("/", _).toUpperCase() + ".json")
   * in the search path
   * 
   * @param key to look up
   * @param searchPath list of folder paths to look in
   */
  def loadSearchpathConfig(
      fullKey:String,
      searchPath:Seq[String] = JsonConfigLoader.searchPath
  ): Option[gson.JsonObject] = {
    val key:String = fullKey.replaceAll("/", "_") + ".json"  
    
    searchPath.map(
        { new java.io.File(_, key) }
      ).find(
        { _.canRead() }
      ).map(
        {
          file => 
          val reader = new java.io.InputStreamReader(
            new java.io.FileInputStream(file), utf8
          )
          try {
            gs.fromJson(reader, classOf[gson.JsonObject])
          } finally {
            reader.close()
          }
        }
      )
  }

  /**
   * Load the config if any from the key.replaceAll("/", "_") system property or
   * environment variable
   */
  def loadEnvConfig(fullKey:String): Option[gson.JsonObject] = {
    val key = fullKey.replaceAll("/", "_") + ".json"
    Option(
      System.getProperty(key, System.getenv(key))
    ).map(
      str => gs.fromJson(str, classOf[gson.JsonObject])
    )
  }

  /**
   * Collect classpath, searchpath, and env config
   * into a sequence
   */
  def loadConfigs(key:String, classLoader:ClassLoader): Seq[gson.JsonObject] =
    Seq(
      loadClasspathConfig(key, classLoader),
      loadSearchpathConfig(key, searchPath),
      loadEnvConfig(key)
    ).flatten

  /**
   * Shallow merge the properties of the given
   * list of objects into a new object in order.
   * Note - does not deep-copy the merged property values
   *
   * @return empty option
   */
  def jsonMerge(objList:Seq[gson.JsonObject]):Option[gson.JsonObject] =
    objList match {
      case x if x.isEmpty => None
      case configSeq => Some(
        configSeq.foldLeft(new gson.JsonObject())(
          (acc, it) => {
            it.entrySet.asScala.foreach(
              {
                entry => acc.add(entry.getKey(), entry.getValue())
              }
            )
            acc
          }
        )
      )
    }

  /**
   * loadConfigs, and do a shallow json merge
   */
  def loadConfig(key:String, classLoader:ClassLoader): Option[gson.JsonObject] = jsonMerge(loadConfigs(key, classLoader))


  /**
   * Bind string constant annotated with given name:
   * Note: tolerates a null binder as a lame mock for testing
   */
  def bindKeyValue(binder: inject.Binder, key:String, value:String): Unit =
      if (null != binder) { 
        binder.bindConstant().annotatedWith(inject.name.Names.named(key)).to(value)
      }

  /**
   * Convenience method - binds all the keys in the given obj via
   * bindKeyValue (above)
   *
   * @return set of keys bound
   */
  def bindKeys(binder: inject.Binder, obj:gson.JsonObject): Set[String] = 
    obj.entrySet.asScala.map(
        {
          it =>
          it.getValue() match {
            case prim: gson.JsonPrimitive if prim.isString() =>
              bindKeyValue(binder, it.getKey(), prim.getAsString())
            case generic =>
              bindKeyValue(binder, it.getKey(), generic.toString())
          }
          it.getKey()
        }
      ).toSet
}
