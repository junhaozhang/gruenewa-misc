/*
 * Copyright (c) 2011 by Alexander Grünewald
 * 
 * This file is part of gruenewa-prelude, a collection of generally
 * useful utility functions.
 *
 * gruenewa-grid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package gruenewa

package object prelude {

  import java.io.{InputStream, OutputStream}

  def using[T <: { def close() }, A](resource: T)(block: T => A): A = {
    try {
      block(resource)
    } finally {
      if (resource != null) {
        resource.close()
      }
    }
  }

  def manage[T <: { def close() }, A](resource: T)(block: => A): A = {
    using(resource) { ignore => block }
  }

  /**
   * Copies the input stream into the output stream.
   */ 
  def transfer(in: InputStream, out: OutputStream) = {
    val buf = new Array[Byte](100*1024)
    var len = 0
    while({len = in.read(buf, 0, buf.length); len != -1}){ 
      out.write(buf, 0, len)
    }
  }

  /**
   * Parser combinator that allows to use pipline syntax when in scope.
   */
  implicit def |>[A](a: =>A) = new {
    def |>[B](f: A => B) = f(a)
  }

  def time[F](f: => F) = {
    val t0 = System.nanoTime
    val result = f
    val t1 = System.nanoTime
    printf("Elapsed: %.9f secs\n",1e-9*(t1-t0))
    result
  }

  def toHex(buf: Array[Byte]): String = 
    buf.map("%02x" format _).mkString

  def checkSum(method: String, inputStream: => InputStream) =  {
    using(inputStream) { is =>
      val bytes = new Array[Byte](4096)
      val md = java.security.MessageDigest.getInstance(method)
      var len = 0;
      do {
        len = is.read(bytes)
        if(len > 0)
          md.update(bytes, 0, len);
      } while (len >= 0)
      
      md.digest()
    }
  }

  def md5(inputStream: => InputStream) = 
    checkSum("MD5", inputStream) |> toHex

  def sha1(inputStream: => InputStream) = 
    checkSum("SHA1", inputStream) |> toHex

}
