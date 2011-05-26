/*
 * Copyright (c) 2010-2011 by Alexander Grünewald
 *
 * This file is part of gruenewa-wsclient.
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
package gruenewa.wsclient

import xml.{ XML, Elem }
import java.net.URL
import java.io.{ InputStream, OutputStream }

case class Service(url: URL) {

  /**
   * Defines a message as a tupel of an XML payload and
   * a map of HTTP transport headers.
   */

  type Message = (Map[String, String], Array[Byte])

  /**
   * Submits a message to the service endpoint.
   *
   * @param message the request containing
   *          the payload and a map of HTTP transport headers
   * @returns response message containing
   *          the response payload and response HTTP headers
   */

  def invoke(message: Message): Message = {

    val (header, payload) = message

    import java.io.DataOutputStream
    import java.io.OutputStreamWriter
    import java.io.InputStreamReader
    import java.net.HttpURLConnection

    val con = url.openConnection.asInstanceOf[HttpURLConnection]

    con.setDoInput(true)
    con.setDoOutput(true)
    con.setUseCaches(false)

    for (h <- header) {
      con.setRequestProperty(h._1, h._2)
    }

    val out = con.getOutputStream

    try {
      out.write(payload)
      out.flush
    } finally {
      out.close
    }

    val responseHeader = parseResponseHeaders(con, 1)

    val in =
      if (con.getResponseCode == 200)
        con.getInputStream
      else con.getErrorStream

    try {
      (responseHeader, slurp(in))
    } finally {
      in.close
    }
  }

  private def slurp(in: java.io.InputStream): Array[Byte] = {

    val out = new java.io.ByteArrayOutputStream()

    try {
      copyStream(in, out)
      out.toByteArray()
    } finally {
      out.close()
    }
  }

  private def parseResponseHeaders(con: java.net.URLConnection, n: Int): Map[String, String] = {

    val key = con.getHeaderFieldKey(n)

    if (key == null)
      Map.empty
    else
      parseResponseHeaders(con, n + 1) ++ Map(key -> con.getHeaderField(key))
  }

  private def copyStream(istream: InputStream, ostream: OutputStream): Unit = {

    var bytes = new Array[Byte](1024)
    var len = -1

    while ({ len = istream.read(bytes, 0, 1024); len != -1 })
      ostream.write(bytes, 0, len)
  }
}

trait SoapDispatch { self: Service =>

  def dispatch(message: Elem, soapAction: Option[String] = None): Elem = {

    import SOAPUtils.{ isSOAP11, isSOAP12 }

    val requestHeader: Map[String, String] =

      if (isSOAP11(message)) {
        getHeaderSOAP11(soapAction)
      } else if (isSOAP12(message)) {
        getHeaderSOAP12(soapAction)
      } else {
        Map.empty
      }

    val requestPayload = message.toString.getBytes("UTF-8")
    val (responseHeader, responsePayload) = self.invoke(requestHeader, requestPayload)
    XML.loadString(new String(responsePayload))
  }

  private def getHeaderSOAP11(action: Option[String]): Map[String, String] = {
    action match {
      case None => Map("Content-Type" -> "text/xml")
      case Some(x) => Map("Content-Type" -> "text/xml", "SOAPAction" -> x)
    }
  }

  private def getHeaderSOAP12(action: Option[String]): Map[String, String] = {
    action match {
      case None => Map("Content-Type" -> "application/soap+xml")
      case Some(x) => Map("Content-Type" -> ("application/soap+xml;action=" + x))
    }
  }
}

object SOAPUtils {

  import xml.{ XML, Elem }

  import javax.xml.soap.{ SOAPMessage, SOAPConstants, MessageFactory }

  private val soap11Factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL)

  private val soap12Factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)

  def isSOAP11(message: Elem): Boolean = message.namespace == "http://schemas.xmlsoap.org/soap/envelope/"

  def isSOAP12(message: Elem): Boolean = message.namespace == "http://www.w3.org/2003/05/soap-envelope"

  def toSOAP(document: Elem): SOAPMessage = {

    import java.io.StringReader
    import javax.xml.transform.Source
    import javax.xml.transform.stream.StreamSource

    val request =
      if (isSOAP12(document)) soap12Factory.createMessage
      else soap11Factory.createMessage

    val reader = new StringReader(document.toString)

    try {
      request.getSOAPPart.setContent(new StreamSource(reader))
      request.saveChanges
      request
    } finally {
      reader.close
    }
  }

  def toElem(message: SOAPMessage): Elem = {

    val out = new java.io.ByteArrayOutputStream()

    try {
      message.writeTo(out)
      XML.loadString(out.toString)
    } finally {
      out.close()
    }
  }
}
