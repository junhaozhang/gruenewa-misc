object Main {
  def main(args: Array[String]) {

    import java.security.KeyStore
    import java.security.PrivateKey
    import java.security.cert.X509Certificate

    import gruenewa.dsig.DSIG

    val xml = <aaa>
                <bbb>Hello World!</bbb>
              </aaa>


    // Load the KeyStore and get the signing key and certificate.
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
