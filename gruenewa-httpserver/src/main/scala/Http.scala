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

import com.sun.net.httpserver.{
  HttpsConfigurator, 
  HttpServer, 
  HttpsServer, 
  HttpContext, 
  HttpExchange, 
  HttpHandler, 
  BasicAuthenticator}

import java.net.InetSocketAddress

// keytool -genkey -alias localhost -keyalg RSA -keystore keystore.jks -keysize 2048

package object http {

  implicit def pimp(ctx: HttpContext) = new RichHttpContext(ctx)

  def HTTP(port: Int = 8080) = {
    val addr  = new InetSocketAddress(port);
    HttpServer.create(addr, port);
  }

  def HTTPS(
    keystoreFile: String = "keystore.jks",
    keystorePass: String = "changeit",
    keyPass: String = "changeit",
    port: Int = 8443) = {

    val keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    val keystore =  KeyStore.getInstance("JKS")
  
    keystore.load(new java.io.FileInputStream(keystoreFile), keystorePass.toCharArray());
    
    keyFactory.init(keystore, keyPass.toCharArray())

    val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)

    trustFactory.init(keystore)

    val ssl = SSLContext.getInstance("TLS")
    ssl.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom())

    val configurator = new HttpsConfigurator(ssl);

    val httpsServer = HttpsServer.create(new InetSocketAddress(port), port);

    httpsServer.setHttpsConfigurator(configurator);

    httpsServer
  }

  class RichHttpContext(underlying: HttpContext) {
    
    def useBasicAuth(realm: String)(check: (String, String) => Boolean) = {
      underlying.setAuthenticator (
	new BasicAuthenticator(realm) {
	  override def checkCredentials(username: String, password: String) = {
	    check(username, password)
	  }
	})
      
      underlying
    }

    def withHandler(handler: HttpExchange => Unit) = {
      underlying.setHandler(new HttpHandler() {
	def handle(exchange: HttpExchange) {
	  handler(exchange)
	}
      })

      underlying
    }
  }
}
