/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.scala

import java.io

/**
 * Some convenience methods for working with java.io
 */
object StreamUtil {

  /**
   * Little tuple for the bufferStream method (below)
   */
  case class DataBuffer[T]( buffer:Array[T], dataSize:Int ) {}

  /**
   * Type for InputStream and Reader
   */
  trait Source[T] {
    def read( buffer:Array[T] ):Int
    def close():Unit
  }

  object Source {
    def apply[T]( readFunc:(Array[T]) => Int, closeFunc:() => Unit ):Source[T] = {
      new Source[T]() {
        override def read( buff:Array[T] ):Int = readFunc( buff )
        override def close = closeFunc
      }
    }
    def apply( stream:io.InputStream ):Source[Byte] = apply( stream.read, stream.close )
    def apply( stream:io.Reader ):Source[Char] = apply( stream.read, stream.close )
  }

  /**
   * Return a Stream backed by reader.readLine
   */
  def readLineStream( reader:io.BufferedReader ):Stream[String] = {
    Option( reader.readLine ) match {
      case Some(line) => Stream.cons( line, readLineStream( reader ) )
      case _ => Stream.empty
    }
  }

  /**
   * Convenience method backed by self-closing BufferedReader that closes when end of file reached
   */
  def readLineStream( source:io.File ):Stream[String] = {
    def readLine( reader:io.BufferedReader ):Stream[String] =
      reader.readLine match {
        case null => {
            reader.close
            Stream.empty
          }
        case line => Stream.cons( line, readLine( reader ) )
      }
    readLine( new io.BufferedReader( new io.FileReader( source ) ))
  }

  /**
   * Map the java.io.Reader|InputStream style streamIn source to
   * a scala Stream for lazy reading
   *
   * @param streamIn source of data fills a given buffer, returns the number
   *                   of items added to the buffer
   * @return Stream that lazily fills buffer - NOTE: does not make sense to
   *             convert .toList or .toSeq since every item in the Stream is
   *             backed by the same buffer array
   */
  def bufferStream[T]( streamIn:( Array[T] ) => Int,
                      buffer:Array[T]
  ):Stream[DataBuffer[T]] = {
    /** Non-closing stream */
    selfClosingBufferStream(
      Source( streamIn, () => {} ),
      buffer )
  }

  /**
   * Map the java.io.Reader|InputStream style streamIn source to
   * a scala Stream for lazy reading
   *
   * @param streamIn source of data fills a given buffer, returns the number
   *                   of items added to the buffer
   * @return Stream that lazily fills buffer - NOTE: does not make sense to
   *             convert .toList or .toSeq since every item in the Stream is
   *             backed by the same buffer array
   */
  def selfClosingBufferStream[T]( streamIn:Source[T],
                                 buffer:Array[T]
  ):Stream[DataBuffer[T]] =     streamIn.read(buffer) match {
    case -1 => Stream.empty
    case x => Stream.cons( DataBuffer( buffer, x ), selfClosingBufferStream( streamIn, buffer ))
  }


  /**
   * Copy the contents of streamIn to streamOut on buffer at a time
   *
   * @param streamIn source function - returns number of items read into buffer
   * @param streamOut sink
   * @return total number of things piped between streams
   */
  def in2Out[T]( streamIn:( Array[T] ) => Int,
                streamOut:( Array[T], Int, Int ) => Unit,
                buffer:Array[T]
  ):Int = {
    (0 /: bufferStream( streamIn, buffer ))(
      (counter:Int, data:DataBuffer[T]) => {
        streamOut( data.buffer, 0, data.dataSize )
        counter + data.dataSize
      }
    )
  }

  /**
   * Convenience method.
   * Copy the contents of streamIn to streamOut 10k at a time
   *
   * @return number of bytes piped between streams
   */
  def in2Out( streamIn:io.InputStream, streamOut:io.OutputStream ):Int =
    in2Out( streamIn.read,
           streamOut.write,
           new Array[Byte]( 10240 )
    )

  /**
   * Convenience method.
   * Copy the contents of streamIn to streamOut 10k characters at a time
   */
  def in2Out( streamIn:io.Reader, streamOut:io.Writer ):Int =
    in2Out( streamIn.read,
           streamOut.write,
           new Array[Char]( 10240 )
    )

  /**
   * Copy source to sink
   * 
   * @return number of bytes copied
   */
  @throws(classOf[io.IOException])
  def copy( source:io.File, sink:io.File ):Int = {
    val in = new io.FileInputStream( source )
    try {
      val out = new io.FileOutputStream( sink )
      try {
        in2Out( in, out )
      } finally {
        out.close
      }
    } finally {
      in.close
    }
  }

  /**
   * Assume UTF-8 encoding
   */
  def readAll( source:io.File ):String = {
    /** Non-closing stream */
    class MyStream( reader:io.Reader ) {
      def read( buf:Array[Char] ):Int = reader.read(buf )
      def close():Unit = reader.close
    }

    (
      (new StringBuilder) /:
      selfClosingBufferStream[Char](
        Source( (new io.InputStreamReader( new io.FileInputStream( source ),
                                  littleware.base.Whatever.UTF8
          )) ),
        new Array[Char]( 10240 )
      )
    )(
      (sb,dataBuffer) => sb.append( dataBuffer.buffer, 0, dataBuffer.dataSize )
    ).toString
  }
}
