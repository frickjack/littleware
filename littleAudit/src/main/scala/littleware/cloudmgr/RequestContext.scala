package littleware.cloudmgr

case class RequestContext(
    requestId: UUID,
    session: Session,
    startTimeMs: Long
)  {}

object RequestContext {

}