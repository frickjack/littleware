package littleware.cloudmgr.service

import java.util.UUID

import scala.util._

import littleware.cloudmgr.{ Cell, Operator }
import littleware.cloudutil.{ RequestContext, Session }


/**
 * Manage the sessions and cells associated with an API.
 * The ApiMgr for each API is hosted at: https://${api}.${cloud.domain}.
 * The cells for each API are hosted at: https://${cellid}.${api}.${cloud.domain}
 * The root web console is at https://conosle.${cloud.domain} - it
 * loads content and configuration from https://console.${api}.${cloud.domain}
 * when serving pages for https://console.${cloud.domain}/${api}/
 */
trait ApiMgr {
    def createCell(cx:RequestContext, name:String, endpoint:String): Cell
    /**
     * New cells
     */
    def setActiveCell(cx:RequestContext, cellId:UUID): Cell

    def getCellById(cx:RequestContext, id:UUID): Option[Cell]
    def getCellByApi(cx:RequestContext): Set[Cell]
    def getCellByProject(cx:RequestContext, projectId:UUID): Set[Cell]

    def getOperators(cx:RequestContext): Set[Operator]
    def addOperator(cx:RequestContext, operator:String): Boolean
    def removeOperator(cx:RequestContext, operator:String): Boolean

    /**
     * API manager knows the mapping of project to cell
     */
    def createSession(cx:RequestContext, projectId:UUID): Option[Session]
    def getSessionById(cx:RequestContext, id:UUID): Option[Session]
    def getSessionsBySubject(cx:RequestContext, subject:String, tmin:Long, tmax:Long): Seq[Session]
    def getSessionsByWindow(cx:RequestContext, tmin:Long, tmax:Long): Seq[Session]
}

object ApiMgr {
    val api = Cell.api
}
