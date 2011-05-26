gruenewa-dsig
=============

Utility library based on javax.xml.crypto.dsig to make it easy to sign
XML documents in Scala.

Example
-------

    import java.security.KeyStore
    import java.security.PrivateKey
    import java.security.cert.X509Certificate

    import gruenewa.dsig.DSIG

    // Fetch Private Key and Certificate 
    val ks = KeyStore.getInstance("JKS")
    ks.load(new java.io.FileInputStream("mykeystore.jks"), "changeit".toCharArray())
    val keyEntry = ks.getEntry("mykey", 
      new KeyStore.PasswordProtection("changeit".toCharArray())).asInstanceOf[KeyStore.PrivateKeyEntry]
    val cert: X509Certificate = keyEntry.getCertificate().asInstanceOf[X509Certificate]
    val key: PrivateKey = keyEntry.getPrivateKey()

    val xml = <aaa>
                <bbb>Hello World!</bbb>
              </aaa>

    // Since we have at least a certificate and a key 
    // we can now sign the document

    val signedXml = DSIG.sign(cert, key)(xml)

    println("******************** SIGNED XML *************************")
    println(signedXml)
    println("******************** SIGNED XML *************************")
    
    // Of course we can also verify a signed document

    val status = DSIG.validate()(signedXml)

    println("******************** VALID? *************************")
    println("Verification Status: " +status)
    println("******************** VALID? *************************")
