package littleware.cloudutil

import com.google.gson
import java.util.UUID

import littleware.scala.GsonProvider
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ emailValidator, notEmptyValidator, notNullValidator, positiveLongValidator }


@gson.annotations.JsonAdapter(classOf[RequestContext.GsonTypeAdapter])
final case class RequestContext(
    requestId: UUID,
    session: Session,
    startTimeMs: Long,
    actor: String,
    // action to resources - for authz
    actionPaths: Map[String, Seq[LRPath]]
)  {}

object RequestContext {
    final class Builder extends PropertyBuilder[RequestContext] {
        val requestId = new Property(UUID.randomUUID()) withName "requestId" withValidator notNullValidator
        val session = new Property[Session](null) withName "session" withValidator notNullValidator
        val startTimeMs = new Property[Long](new java.util.Date().getTime()) withName "startTimeMs" withValidator positiveLongValidator
        val actor = new Property[String]() withName "actor" withValidator notNullValidator
        val actionPaths = new Property[Map[String, Seq[LRPath]]](Map.empty) withName "actionPaths" withValidator notEmptyValidator

        override def copy(value:RequestContext):this.type = requestId(value.requestId
        ).session(value.session
        ).startTimeMs(value.startTimeMs
        ).actor(value.actor
        ).actionPaths(value.actionPaths)

        override def build():RequestContext = {
            this.validate()
            RequestContext(requestId(), session(), startTimeMs(), actor(), actionPaths())
        }
    }

    class GsonTypeAdapter extends gson.TypeAdapter[RequestContext]() {
        override def read(reader:gson.stream.JsonReader):RequestContext = {
            val builder = new Builder()
            GsonProvider.objectIterator(reader).foreach(
                {
                    kv =>
                    kv._1 match {
                    case "requestId" => {
                        builder.requestId(UUID.fromString(reader.nextString()))
                    }
                    case "session" => {
                        builder.session(Session.gsonTypeAdapter.read(reader))
                    }
                    case "startTimeMs" => {
                        builder.startTimeMs(reader.nextLong())
                    }
                    case "actor" => {
                        builder.actor(reader.nextString())
                    }
                    case "actionPaths" => {
                        builder.actionPaths(
                            GsonProvider.objectIterator(reader).map(
                                {
                                    kv =>
                                    kv._1 -> GsonProvider.arrayIterator(reader).map(
                                            _ => LRN.uriToLRN(new java.net.URI(reader.nextString())).asInstanceOf[LRPath]
                                        ).toSeq
                                }
                            ).toMap
                        )
                    }
                    }
                }
            )
            builder.build()
        }

        override def write(writer:gson.stream.JsonWriter, value:RequestContext):Unit = {
            writer.beginObject(
            ).name("requestId").value(value.requestId.toString()
            ).name("session")
            Session.gsonTypeAdapter.write(writer, value.session)
            writer.name("startTimeMs").value(value.startTimeMs
            ).name("actionPaths").beginObject()
            value.actionPaths.foreach(
                {
                    kv =>
                    writer.name(kv._1).beginArray()
                    kv._2.foreach(
                        resourcePath => writer.value(LRN.lrnToURI(resourcePath).toString())
                    )
                    writer.endArray()
                }
            )
            writer.endObject().endObject()
        }
    }

    //littleware.scala.GsonProvider.registerTypeAdapter(classOf[RequestContext], GsonTypeAdapter)
}
