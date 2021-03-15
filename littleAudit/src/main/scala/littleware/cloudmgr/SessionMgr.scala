package littleware.cloudmgr

import java.util.UUID
import scala.util.Try
import io.jsonwebtoken

/**
 * Helper manages pickling of Session and JWT -
 * including signature generation and validation
 */
trait SessionMgr {
    /**
     * @param jwtIdToken authn credential - id token for either Cognito user or little-cloud robot
     * @return session ready to encode as JWT
     */
    def startSession(jwtIdToken:String, projectId:UUID, api:String):Session
    
    def validateToken(jwtToken:String):Boolean

    def sessionToJwt(session:Session):String
    def jwtToSession(jwt:jsonwebtoken.Jws[String]):Try[Session]
}
