/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.s3Copy.model

/**
 * Just a common-trait for FolderSummary and ObjectSummary
 */
trait PathSummary {
  val path:java.net.URI
}
