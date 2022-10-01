package littleware.cloudutil

import java.util.{ Date, UUID }
import math.Ordered.orderingToOrdered


case class TimeId (id: UUID, timestamp:Long) extends Comparable[TimeId] {

    override def compareTo(other:TimeId): Int = {
        if (timestamp != other.timestamp) {
            java.lang.Long.compare(timestamp, other.timestamp)
        } else {
            id.compareTo(other.id);
        }
    }

    override def toString(): String = {
        TimeId.pad20(this.timestamp, 20) + ":" + id
    }
}

object TimeId {
    val zeroId:UUID = new java.util.UUID(0,0)
    
    def apply(timestamp: Long):TimeId = new TimeId(zeroId, timestamp)

    /**
     * Return a string that pads the given num to paddedLength characters
     * by prefixing zeros.  The paddedLength must be less than 20,
     * and the number may not have more than paddedLength digits in it
     */
    def pad20(num:Long, paddedLength:Int):String = {
        if (paddedLength > 20) {
            throw new IllegalArgumentException("max pad length is 20, got: " + paddedLength)
        }
        val in = num.toString()
        if (in.length() > 20) {
            throw new IllegalArgumentException("padding input exceeds 14 characters: " + in);
        }
        val temp = "000000000000000000000000" + in;
        temp.substring(temp.length() - 20);
    }

    def now():TimeId = TimeId(UUID.randomUUID(), new Date().getTime())

    def fromString(value:String):TimeId = {
        val parts = value.split(":")
        TimeId(UUID.fromString(parts(1)), parts(0).toLong)
    }
}
