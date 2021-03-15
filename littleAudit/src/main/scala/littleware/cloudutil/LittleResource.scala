package littleware.cloudutil

trait LittleResource extends littleware.base.cache.CacheableObject {
    val id:java.util.UUID
    val updateTime:Long
    val lrn:java.net.URI

    override def getTimestamp() = updateTime
    override def getId() = id
    override def hashCode() = id.hashCode()
}