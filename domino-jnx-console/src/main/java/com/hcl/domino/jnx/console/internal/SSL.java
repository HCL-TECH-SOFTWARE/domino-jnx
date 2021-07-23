/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jnx.console.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Loads the Java keystore with the certificate to connect to the SSL server controller socket.
 */
class SSL {
	private static SSLContext sslctx = null;
	private static Class<?> parentClass;

	private SSL() {
	}

	public static void setClassLoader(Class<?> class_) {
		parentClass = class_;
	}

	private static void checkStrongCiphersAvailable() {
		int n = 0;
		try {
			Cipher cipher = Cipher.getInstance("AES");
			n = Cipher.getMaxAllowedKeyLength("AES");
		}
		catch (Exception e) {
			throw new RuntimeException("Error checking for required JVM ciphers", e);
		}

		if (n < 256) {
			throw new RuntimeException("WARNING: Attempting to use AES_256, but the JVM only supports a maximum key length of " + n + ". The JVM should be upgraded to use the JCE unlimited strength jars.");
		}
	}

	private static synchronized void initSSLContext() {
		if (sslctx != null) {
			return;
		}

		SSL.checkStrongCiphersAvailable();

		try {
			char[] arrc = "andhrawalabrave<3".toCharArray();

			String resourcePath = "/" + DominoConsoleRunner.class.getPackage().getName().replace('.', '/') + "/jconsole.jks";

			KeyStore keyStore = KeyStore.getInstance("jks");
			InputStream in = parentClass.getResourceAsStream(resourcePath);
			if (in == null) {
				throw new RuntimeException("Required file " + resourcePath + " not found");
			}

			keyStore.load(in, arrc);

			//FIXED: Added fallback to SunX509, code did not work when usin OpenJ9
			KeyManagerFactory keyManagerFactory;
			try {
				keyManagerFactory = KeyManagerFactory.getInstance("IbmX509");
			}
			catch (NoSuchAlgorithmException e2) {
				keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			}
			keyManagerFactory.init(keyStore, arrc);

			TrustManagerFactory trustManagerFactory;
			try {
				trustManagerFactory = TrustManagerFactory.getInstance("IbmX509");
			}
			catch (NoSuchAlgorithmException e2) {
				trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			}
			trustManagerFactory.init(keyStore);
			sslctx = SSLContext.getInstance("TLS");
			sslctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		}
		catch (Exception e) {
			throw new RuntimeException("Error initializing SSL context to access Domino console", e);
		}
	}

	public static SSLSocket getClientSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		initSSLContext();

		SSLSocketFactory sSLSocketFactory = sslctx.getSocketFactory();
		SSLSocket sSLSocket = null;
		try {
			sSLSocket = (SSLSocket)sSLSocketFactory.createSocket(host, port, localHost, localPort);
			sSLSocket.startHandshake();
		} catch (IOException iOException) {
			iOException.printStackTrace();
			if (sSLSocket != null) {
				try {
					sSLSocket.close();
				} catch (IOException iOException1) {}
			} 
			sSLSocket = null;
			throw iOException;
		} 
		return sSLSocket;
	}
}

