package littleware.cloudmgr.service.lambda


object LambdaHelper {

    /**
     * @param cookieStr a=v; b=v; c=v
     * @return key:value map
     */
    def parseCookies(cookieStr: String): Map[String, String] = 
        cookieStr.split(raw";\s*").map(
            s => s.split("=")
        ).filter({ _.length == 2}
        ).map(kv => kv(0) -> kv(1)
        ).toMap

    case class CookieInfo (
        name: String,
        value: String,
        domain: Option[String],
        ttlSecs: Int
    ) {
        override def toString():String = {
            val sb = new StringBuilder().append(
                s"${name}=${value}; Path=/; Secure; HttpOnly; SameSite=Strict"
            )
            if (ttlSecs < 0) {
                sb.append("; Expires=Thu, 01 Jan 1970 00:00:00 GMT")
            } else if (ttlSecs > 0) {
                sb.append(s"; Max-Age=${ttlSecs}")
            }
            domain.foreach(name => sb.append(s"; Domain=${name}"))
            sb.toString()
        }
    }

}