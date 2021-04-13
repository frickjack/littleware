package littleware.cloudutil

import java.util.UUID


case class RequestContext(
    requestId: UUID,
    session: Session,
    startTimeMs: Long
)  {}

object RequestContext {

}