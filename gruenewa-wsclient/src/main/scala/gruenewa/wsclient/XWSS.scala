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

import scala.xml.Elem
import com.sun.xml.wss.XWSSProcessor
import com.sun.xml.wss.XWSSProcessorFactory
import javax.security.auth.callback.CallbackHandler

trait WSSProcessor {
  def secureOutgoing(message:Elem):Elem
  def verifyIncoming(message:Elem):Elem
}

/**
 * <p>
 *   <strong>Example use</strong>
 * </p>
 * <pre>
 *  val message = 
 *    <soapenv:Envelope  xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
 *	    <soapenv:Body>
 *        <sim:echo  xmlns:sim="http://simple/">hi there!</sim:echo>
 *    	</soapenv:Body>
 *    </soapenv:Envelope>
 * 
 *  val xwss = XWSS {
 *    <xwss:SecurityConfiguration xmlns:xwss="http://java.sun.com/xml/ns/xwss/config" dumpMessages="false">
 *	    <xwss:UsernameToken name="user123" password="pass123" useNonce="true" digestPassword="true" />
 * 	  </xwss:SecurityConfiguration>
 *  }
 *  
 *  val securedMessage = xwss.secureOutgoing(message)
 *  </pre>  
 */
object XWSS {

  def apply(config:Elem):WSSProcessor = this(config, null)
  
  def apply(config:Elem, handler:CallbackHandler):WSSProcessor = {
     val in = new java.io.ByteArrayInputStream(config.toString.getBytes)

      val xwss = 
        try {
        	XWSSProcessorFactory.newInstance.createProcessorForSecurityConfiguration(in, handler)
        } finally {
        	in.close()
        }
     return new XWSS(xwss)    
  }
}

class XWSS(xwss:XWSSProcessor) extends WSSProcessor {
  
  import gruenewa.wsclient.SOAPUtils.{toSOAP, toElem}
  
  def secureOutgoing(message:Elem):Elem = {
     val soap = toSOAP(message)
     xwss.secureOutboundMessage(xwss.createProcessingContext(soap))
     soap.saveChanges
     toElem(soap)
  }
   
  def verifyIncoming(message:Elem):Elem = {
     val soap = toSOAP(message)
     xwss.verifyInboundMessage(xwss.createProcessingContext(soap))
     soap.saveChanges
     toElem(soap)
  }
}
