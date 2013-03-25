/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.s3Copy
package controller


import java.{io => jio}
import java.net.URI


/**
 * Tool for listing and comparing paths, and copying
 * objects from one path to another
 */
trait PathTool {
  /**
   * Return the FolderSummary or ObjectSummary associated
   * with the given path
   */
  def ls( path:URI ):Option[model.PathSummary]
  /**
   * Same as ls, but recurses on subfolders to provide
   * a FolderSummary stream if path references a folder
   */
  def lsR( path:URI ):Stream[model.PathSummary]
  
  /**
   * Copy the object at source to dest - overwrites dest if it exists.
   * NOOP (returns None) if source does not exist or references a folder.
   * 
   * @param source object to copy (must be a file)
   * @param dest file-path to copy over
   * @return result of ls(dest) after copy
   */
  def copy( source:URI, dest:URI ):Option[model.ObjectSummary]
  
  /**
   * Copy source file to destFolder/sourceName
   */
  def copyUnder( source:URI, destFolder:URI ):Option[model.ObjectSummary]
}

object PathTool {
  
  /**
   * Little helper to extract the "base-name" of the given path:
   *     a:/b/c == c
   */
  def baseName( uri:java.net.URI ):String =
    new jio.File( uri.getPath ).getName
  
 
  /**
   * Little helper assembles a URI to identify the given S3 object
   */
  def s3Path( bucketName:String, key:String ):java.net.URI =
    new java.net.URI( "s3", bucketName, ("/" + key).replaceAll( "//+", "/" ), null )
}