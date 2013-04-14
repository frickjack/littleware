/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.scala

/**
 * Little helper for processing command-line arguments
 */
object GetoptHelper {
  /**
   * Group args according to "-+name" prefix group
   */
  def extract( args:Seq[String] ):Map[String,Seq[String]] = {
    (( "", Map.empty[String,Seq[String]]) /: args)(
        (argGroup,arg) => argGroup match { 
            case (currentGroup,allGroups) => {
              val gname = if ( arg.startsWith( "-" ) ) arg.replaceAll( "^-+", "" ) else currentGroup
              val newGroups = if( gname != currentGroup ) {}
              (
                  gname,
                  allGroups + (gname -> allGroups.get(gname).toSeq.flatMap( (gseq) => if( arg.startsWith( "-" ) ) gseq else gseq :+ arg ))
              )
            } 
          }
        )._2
  }

  
  /**
   * Compress a set of options down by combining
   * equivalent option flags - for example, an
   * group of flags might include: "help" -> ["h", "info"],
   * so the output would just concatenate the opts values
   * so "help" -> opts["help"] + opts["h"] + opts["info"] ...
   * something like that.
   */
  def compress( opts:Map[String,Seq[String]], groups:Map[String,Iterable[String]] ):Map[String,Seq[String]] = {
    groups.map( _ match {
      case (group,aliases) => group -> 
      (group +: aliases.toSeq).distinct.flatMap( opts.get(_) ).flatten
    })
  }
}
