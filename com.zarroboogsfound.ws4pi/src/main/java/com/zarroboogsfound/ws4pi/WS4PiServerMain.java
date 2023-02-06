/**
 * 
 */
package com.zarroboogsfound.ws4pi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author pi
 *
 */
public class WS4PiServerMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// load the configuration file and fire up the server
    	WS4PiConfig config = null;
		try {
			String hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
			config = WS4PiConfig.load("config/config.json");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		try {
			config.setSslContext(createSslContext(config.getKeyStoreFile(), config.getPassword()));
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new ShutdownHook(config));
		
		WS4PiServer server = new WS4PiServer(config);
		server.start();
		
		
		String s = System.getProperty("runInEclipse");
		if (Boolean.parseBoolean(s)) {
		    System.out.println("You're using Eclipse; click in this console and " +
		            "press ENTER to call System.exit() and run the shutdown routine.");
		    try {
		        System.in.read();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    System.out.println("Calling shutdown hooks and exiting!");
		    System.exit(0);
		}
		
		while (server.isRunning()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static SSLContext createSslContext(String keystoreFile, String password)
			throws GeneralSecurityException, IOException {
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		try (InputStream in = new FileInputStream(keystoreFile)) {
			keystore.load(in, password.toCharArray());
		}
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keystore, password.toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keystore);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

		return sslContext;
	}
}
