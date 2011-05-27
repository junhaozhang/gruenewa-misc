/*
 * Copyright (c) 2011 by Alexander Grünewald
 * 
 * This file is part of gruenewa-streams, a collection of generally
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

import java.io.{InputStream, OutputStream}

package object streams {

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

  def toHex(buf: Array[Byte]): String = 
    buf.map("%02x" format _).mkString

  def checksum(method: String, inputStream: () => InputStream) =  {

    val is = inputStream()

    try {
      val bytes = new Array[Byte](4096)
      val md = java.security.MessageDigest.getInstance(method)
      var len = 0;
      do {
        len = is.read(bytes)
        if(len > 0)
          md.update(bytes, 0, len);
      } while (len >= 0)
      
      md.digest()
    } finally {
      is.close()
    }
  }

  def md5(inputStream: () => InputStream) = 
    toHex(checksum("MD5", inputStream))

  def sha1(inputStream: () => InputStream) = 
    toHex(checksum("SHA1", inputStream))

}
