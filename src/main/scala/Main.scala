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

object Main {
  def main(args: Array[String]) {

    import java.security.KeyStore
    import java.security.PrivateKey
    import java.security.cert.X509Certificate

    import gruenewa.dsig.DSIG

    val xml = <aaa>
                <bbb>Hello World!</bbb>
              </aaa>

    val ks = KeyStore.getInstance("JKS");
    ks.load(new java.io.FileInputStream("mykeystore.jks"), "changeit".toCharArray());
    val keyEntry = ks.getEntry("mykey", new KeyStore.PasswordProtection("changeit".toCharArray())).asInstanceOf[KeyStore.PrivateKeyEntry];
    val cert: X509Certificate = keyEntry.getCertificate().asInstanceOf[X509Certificate];
    val key: PrivateKey = keyEntry.getPrivateKey()

    val signedXml = DSIG.sign(cert, key)(xml)

    println("******************** SIGNED *************************")
    println(signedXml)
    println("******************** SIGNED *************************")
    
    val valid = DSIG.validate()(signedXml)

    println("******************** VALID? *************************")
    println(valid)
    println("******************** VALID? *************************")
    
    println("Done")

  }
}
