package gruenewa


import java.io._

import javax.net.ssl.{SSLContext, KeyManagerFactory, TrustManagerFactory}
import java.security.{KeyStore, SecureRandom}

import com.sun.net.httpserver.{HttpsConfigurator, HttpServer, HttpsServer, HttpExchange => HttpEx, HttpHandler, BasicAuthenticator}

import java.net.InetSocketAddress

// keytool -genkey -alias localhost -keyalg RSA -keystore keystore.jks -keysize 2048

package object http {

  type HttpExchange = HttpEx

  def createHttp(port: Int = 8080) = {
    val addr  = new InetSocketAddress("localhost", 8000);
    HttpServer.create(addr, port);
  }

  def createHttps(
    keystoreFile: String = "keystore.jks",
    keystorePass: String = "changeit",
    keyPass: String = "changeit",
    port: Int = 8080) = {

    val keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    val keystore =  KeyStore.getInstance("JKS")
  
    keystore.load(new java.io.FileInputStream(keystoreFile), keystorePass.toCharArray());
    
    keyFactory.init(keystore, keyPass.toCharArray())

    val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)

    trustFactory.init(keystore)

    val ssl = SSLContext.getInstance("TLS")
    ssl.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom())

    val configurator = new HttpsConfigurator(ssl);

    val httpsServer = HttpsServer.create(new InetSocketAddress("localhost", port), port);

    httpsServer.setHttpsConfigurator(configurator);

    httpsServer
  }

  implicit def wrapHttpHandler(f: HttpExchange => Unit) = {
    new HttpHandler() {
      override def handle(e: HttpExchange) = f(e)
    }
  }

  implicit def wrapBasicAuthenticator(f: (String, String) => Boolean) = {
    new BasicAuthenticator("/") {
      override def checkCredentials(username: String, password: String) = {
	f(username, password)
      }
    }
  }
}
