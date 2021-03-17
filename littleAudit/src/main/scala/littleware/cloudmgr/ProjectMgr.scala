package littleware.cloudmgr

import java.util.UUID
import scala.util._

import littleware.cloudutil.{ RequestContext, Session }


/**
 * Manage projects, api's, cells
 */
trait ProjectMgr {
    def createProject(cx:RequestContext, name:String): Project
    def updateProjectOwners(cx:RequestContext, projId:UUID, owners:Set[String]): Project
    def enableProjectApi(cx:RequestContext, projId:UUID, api:String): Project
    def disableProject(cx:RequestContext, projId:UUID): Project

    def getProjectById(cx:RequestContext, id:UUID): Option[Project]
    def getProjectByOwner(cx:RequestContext, owner:String): Seq[Project]

    def createCell(cx:RequestContext, name:String, endpoint:String): Cell
    
    def getCellById(cx:RequestContext, id:UUID): Option[Cell]
    def getCellByProject(cx:RequestContext, projectId:UUID): Seq[Cell]

    def getOperators(cx:RequestContext): Seq[Operator]
    def addOperator(cx:RequestContext, operator:String): Boolean
    def removeOperator(cx:RequestContext, operator:String): Boolean

    /**
     * Manager knows the mapping of project to cell
     */
    def createSession(cx:RequestContext, projectId:UUID, api:String): Try[Session]
    /**
     * Owner or operator can do this - isOwner set true in session
     */
    def createAdminSession(cx:RequestContext, projectId:UUID): Option[Session]

    /* Do this kind of thing later ...
    should be part of analytics API or something ...
    def getSessionById(cx:RequestContext, id:UUID): Option[Session]
    def getSessionsBySubject(cx:RequestContext, subject:String, tmin:Long, tmax:Long): Seq[Session]
    def getSessionsByWindow(cx:RequestContext, tmin:Long, tmax:Long): Seq[Session]
    */
}
