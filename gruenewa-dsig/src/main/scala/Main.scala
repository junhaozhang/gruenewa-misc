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
    
    val test1 = DSIG.validate()(signedXml)

    println("******************** VALID? *************************")
    println(test1)
    println("******************** VALID? *************************")
    
    val invalidSigXml = <aaa>
                <bbb>Hello World!</bbb>
              <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"></CanonicalizationMethod><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"></SignatureMethod><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"></Transform></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"></DigestMethod><DigestValue>8s1QE7SNsfpVMUNy1CAf8rQUsvY=</DigestValue></Reference></SignedInfo><SignatureValue>Lcm6CwJPAq+adXjfpFmMZIjwqvq3FYYxLL33/1rbJwphsmY92fJg9sZKKaPkMhOBXzLSXUq2cnDS
CmquEDm3frEA0HWigDZTLYjGL3cJ8k/WE4+qKV6LEKH/t7UhJHONSyEAATQM9w4ZgszpOHXMI2cq
NzTyV4NLih1ZVaV858KNmX7kKp1TpLAxDEJ6rA4hUL5Ij/SGryYexV7WrWC0jmJ0hDVTqKVg6JnM
LmgK1nrvSoDEYRYnUapGMzr3tQE47F4zmeuX3OS80o3Y2Osh15+oXFPMaXgFozvA5aK21I7Rma8o
i3y6W7wVuNdFXlduCVaWBxjbtYNO1HT01LmzPg==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=Alexander Gruenewald,OU=Privat,O=Privat,L=Berlin,ST=Berlin,C=DE</X509SubjectName><X509Certificate>MIIDXDCCAkSgAwIBAgIETULXljANBgkqhkiG9w0BAQUFADBwMQswCQYDVQQGEwJERTEPMA0GA1UE
CBMGQmVybGluMQ8wDQYDVQQHEwZCZXJsaW4xDzANBgNVBAoTBlByaXZhdDEPMA0GA1UECxMGUHJp
dmF0MR0wGwYDVQQDExRBbGV4YW5kZXIgR3J1ZW5ld2FsZDAeFw0xMTAxMjgxNDQ5NThaFw0xMTA0
MjgxNDQ5NThaMHAxCzAJBgNVBAYTAkRFMQ8wDQYDVQQIEwZCZXJsaW4xDzANBgNVBAcTBkJlcmxp
bjEPMA0GA1UEChMGUHJpdmF0MQ8wDQYDVQQLEwZQcml2YXQxHTAbBgNVBAMTFEFsZXhhbmRlciBH
cnVlbmV3YWxkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzwxPTc8+STiEhNt1T3Ic
obJsB3w+UAX5hbILv9W9U1qXT2GK5DhXkxY5OKtPUSXygtSWfCXUNmRk7cw/vw3i3fomWFYRq+Ya
7SCyi225TQOmZhsx8Rp8Bg7TdjjkdSPRs2PFKo9PEd6XprHWGS0p4mUqm0HDdbI5FumhcbhPx6hv
NtULFUs7KkSYmbWSEntzRqgSIL54nMTwjidjPyZ3WxpicaKvabt0oFCzHJEYJMhntXzPTq/uos1k
6h+sh50+fDsmWR+dZUkAfXxZBgbcd5I7LWPOtLPdpb5M84WszrLp2mTyItwHijKHI/MaWZMFqNOH
sli2qFhXJI617gt8EwIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQBiyoEp/ZY5oe4qNHcHlxYA58Au
qKdNEs4FKDHs15270xmaxCTSmt/zuF002FAyY5XimT/HTsvzNCNYbjJukK61oj75xUFQ7XkleoC/
m1vClSawRQGNNnHc97c41OGPSJA598q+4Bgm8HuMlp/ert1vLyLCidX6PPt0yvK7KrPdeI6tEQtW
9dhF2J/e6pByVwFIxmUXngDRQjVGxUfXwyRwAs50VCSYZGOE230SCbol7ftdy6DuOHgXnxCeTAdO
ltQxOOOTpwRFxgl6hLMtojOAMhc7ITa+97pjDRZot+zb1mQzilpPcdm/HDMiHBo7YoURTnmwu3Ta
A6Z5CiisnJd9</X509Certificate></X509Data></KeyInfo></Signature></aaa>

    val test2 = DSIG.validate()(invalidSigXml)

    println("******************** VALID? *************************")
    println(test2)
    println("******************** VALID? *************************")

    val test3 = DSIG.validate()(<aaa/>)

    println("******************** VALID? *************************")
    println(test3)
    println("******************** VALID? *************************")
    
    println("Done")

  }
}
