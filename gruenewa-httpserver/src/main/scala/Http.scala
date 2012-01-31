/*
 * Copyright (c) 2012 by Alexander Grünewald
 * 
 * This file is part of gruenewa-httpserver, a collection of generally
 * useful utility functions.
 *
 * gruenewa-httpserver is free software: you can redistribute it and/or modify
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


import java.io._

import javax.net.ssl.{SSLContext, KeyManagerFactory, TrustManagerFactory}
import java.security.{KeyStore, SecureRandom}

import com.sun.net.httpserver.{HttpsConfigurator, HttpServer, HttpsServer, HttpExchange => HttpEx, HttpHandler, BasicAuthenticator}

import java.net.InetSocketAddress

// keytool -genkey -alias localhost -keyalg RSA -keystore keystore.jks -keysize 2048

package object http {

  type HttpExchange = HttpEx

  def createHttpServer(port: Int = 8080) = {
    val addr  = new InetSocketAddress("localhost", port);
    HttpServer.create(addr, port);
  }

  def createHttpsServer(
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

  def createBasicAuth(f: (String, String) => Boolean)(path: String) = {
    new BasicAuthenticator(path) {
      override def checkCredentials(username: String, password: String) = {
	f(username, password)
      }
    }
  }
}
