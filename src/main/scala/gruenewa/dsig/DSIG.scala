/*
 * Copyright (c) 2011 by Alexander Grünewald
 * 
 * This file is part of gruenewa-dsig, a XML signature utility library.
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
package gruenewa.dsig

import ARM.using

object DSIG {

  import javax.xml.crypto._
  import javax.xml.crypto.dom._
  import javax.xml.crypto.dsig._
  import javax.xml.crypto.dsig.dom._
  import javax.xml.crypto.dsig.keyinfo._
  import javax.xml.crypto.dsig.spec._

  import java.security.PrivateKey
  import java.security.cert.X509Certificate

  import java.util.Collections.singletonList

  import scala.xml.Node

  private lazy val sigFactory =  XMLSignatureFactory.getInstance("DOM")

  def sign(certificate: X509Certificate, 
           privateKey: PrivateKey, 
           digestMethod: String = DigestMethod.SHA1, 
           signatureMethod: String = SignatureMethod.RSA_SHA1, 
           c14nMethod: String = CanonicalizationMethod.INCLUSIVE)(xml: Node): Node = {
    
    val digestAlg = sigFactory.newDigestMethod(digestMethod, null)
    val transform = sigFactory.newTransform(Transform.ENVELOPED, null.asInstanceOf[TransformParameterSpec])
    val reference = sigFactory.newReference("", digestAlg, singletonList(transform), null, null)

    val signatureAlg = sigFactory.newSignatureMethod(signatureMethod, null)
    val c14nAlg = sigFactory.newCanonicalizationMethod(c14nMethod, null.asInstanceOf[C14NMethodParameterSpec]) 
    val signedInfo = sigFactory.newSignedInfo(c14nAlg, signatureAlg, singletonList(reference))

    val keyInfoFactory = sigFactory.getKeyInfoFactory()
    val x509Content = new java.util.ArrayList[AnyRef]()
    x509Content.add(certificate.getSubjectX500Principal().getName())
    x509Content.add(certificate)
    val x509Data = keyInfoFactory.newX509Data(x509Content)
    val keyInfo = keyInfoFactory.newKeyInfo(singletonList(x509Data))

    val doc = DOMUtils.asDOC(xml)

    val context = new DOMSignContext(privateKey, doc.getDocumentElement())
    
    val signature = sigFactory.newXMLSignature(signedInfo, keyInfo)
  
    signature.sign(context)

    DOMUtils.asXML(doc)
  }

  def validate(keySelector: KeySelector = X509KeySelector)(xml: Node) = {

    val doc = DOMUtils.asDOC(xml)

    val elems = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature")
    if (elems.getLength() == 0) {
      throw new Exception("Cannot find Signature")
    }

    val valContext = new DOMValidateContext(keySelector, elems.item(0))
  
    val signature = sigFactory.unmarshalXMLSignature(valContext)

    val validationSuccess = signature.validate(valContext)

    if (!validationSuccess) {
      println("Signature validation failed")
      val sv = signature.getSignatureValue().validate(valContext)
      println("signature validation status: " + sv)
      if (!sv) {

        val i = signature.getSignedInfo().getReferences().iterator()
        var j = 0
        while (i.hasNext()) {
          val refValid = i.next().asInstanceOf[Reference].validate(valContext)
          println("ref["+j+"] validity status: " + refValid)
          j += 1 
        }
      }
    }

    validationSuccess
  }
}


object X509KeySelector extends javax.xml.crypto.KeySelector {

  import javax.xml.crypto._
  import javax.xml.crypto.dom._
  import javax.xml.crypto.dsig._
  import javax.xml.crypto.dsig.dom._
  import javax.xml.crypto.dsig.keyinfo._
  import javax.xml.crypto.dsig.spec._

  import java.security.cert.X509Certificate

  def select(keyInfo: KeyInfo, purpose: KeySelector.Purpose, method: AlgorithmMethod, context: XMLCryptoContext): KeySelectorResult =  {
    val ki = keyInfo.getContent().iterator()
    while (ki.hasNext()) {
      val info = ki.next().asInstanceOf[XMLStructure]
      if (info.isInstanceOf[X509Data]) {
        val x509Data = info.asInstanceOf[X509Data]
        val xi = x509Data.getContent().iterator()
        while (xi.hasNext()) {
          val o = xi.next()
          if (o.isInstanceOf[X509Certificate]) {
            val key = o.asInstanceOf[X509Certificate].getPublicKey()
            if (checkAlgorithm(method.getAlgorithm(), key.getAlgorithm())) {
              return new KeySelectorResult() {
                def getKey() = key
              }
            }
          }
        }
      }
    }
    throw new KeySelectorException("Couln't find matching key!")
  }

  def checkAlgorithm(uri: String, name: String) = {
    (name.equalsIgnoreCase("DSA") &&
     uri.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) ||
    (name.equalsIgnoreCase("RSA") &&
     uri.equalsIgnoreCase(SignatureMethod.RSA_SHA1))
  }
}

object DOMUtils {
  import scala.xml.{Node, XML}
  import org.xml.sax.InputSource

  import javax.xml.parsers.DocumentBuilderFactory
  import javax.xml.transform.TransformerFactory
  import javax.xml.transform.dom.DOMSource
  import javax.xml.transform.stream.StreamResult

  import java.io.{StringReader, 
                  ByteArrayInputStream => BIS, 
                  ByteArrayOutputStream => BOS}

  val documentBuilder = DocumentBuilderFactory.newInstance()
  documentBuilder.setNamespaceAware(true)

  def asDOC(xml: Node) =
    documentBuilder.newDocumentBuilder().parse(new InputSource(new StringReader(xml.toString)))

  def asXML(doc: org.w3c.dom.Document) = {
    val transformer = TransformerFactory.newInstance.newTransformer()
    using(new BOS) { os =>
      transformer.transform(new DOMSource(doc), new StreamResult(os))
      using(new BIS(os.toByteArray)) { in =>
        XML.load(in)
      }
    }
  }
}

object ARM {

  type Closable = { def close() }

  def using[T <: Closable, A](resource: T)(block: T => A): A = {
    try {
      block(resource)
    } finally {
      if (resource != null) {
        resource.close()
      }
    }
  }
}
