/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Regulated by Software License
 */
package littleware.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import littleware.base.PropertiesLoader;

/**
 * Specialization of UnicastRemoteObject that simplifies
 * experimentation with SocketFactory.
 */
public class LittleRemoteObject extends UnicastRemoteObject {

    private static final Logger log = Logger.getLogger(LittleRemoteObject.class.getName());
    private static final int registryPort;

    static {
        int port = 1239;

        try {
            final Properties userProps = PropertiesLoader.get().loadProperties();

            final String portOverride = userProps.getProperty("int.lw.rmi_port");

            if (null != portOverride) {
                try {
                    port = Integer.parseInt(portOverride);
                } catch (NumberFormatException e) {
                    log.log(Level.INFO, "Failure parsing int.lw.rmi_port system property, caught: " + e, e);
                }
            }
        } catch (java.io.IOException ex) {
            log.log(Level.SEVERE, "Unable to read server properties", ex);
        }
        registryPort = port;
    }

    public static int getRegistryPort() {
        return registryPort;
    }
    //---------------------------------
    private static SSLSocketFactory sslSocketFactory;
    private static SSLServerSocketFactory sslServerSocketFactory;

    static {

        // Create a trust manager that does not validate certificate chains
        //     http://exampledepot.com/egs/javax.net.ssl/TrustAll.html
        final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };


        // Install the all-trusting trust manager
        try {
            final char[] storepass = "password".toCharArray();
            final char[] keypass = "password".toCharArray();
            final String storename = "littleKeystore.jks";

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            final KeyStore ks = KeyStore.getInstance("JKS");
            {
                final InputStream in = LittleRemoteObject.class.getClassLoader().getResourceAsStream("littleware/net/littleKeystore.jks");
                try {
                    ks.load(in, storepass);
                } finally {
                    in.close();
                }
            }
            kmf.init(ks, keypass);

            final SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
            sslServerSocketFactory = sc.getServerSocketFactory();
            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            //clientSocketFactory = new MyClientSocketFactory( sc.getSocketFactory() );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to setup trusing SSL socket factory", ex);
            throw new IllegalStateException("Failed to setup RMI socket factory", ex);
        }

    }

    /**
     * Internal serializable socket factory that does not check server certificates
     */
    public static class MyClientSocketFactory extends SslRMIClientSocketFactory {

        public MyClientSocketFactory() {
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return sslSocketFactory.createSocket(host, port);
        }

        @Override
        public boolean equals(Object other) {
            return (other != null) && (other instanceof MyClientSocketFactory);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }
    }
    private static final SslRMIServerSocketFactory serverSocketFactory = new SslRMIServerSocketFactory() {
        @Override
        public ServerSocket createServerSocket( int port ) throws IOException {
            return sslServerSocketFactory.createServerSocket(port);
        }
    };
    
    //new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"}, new String[]{"TLSv1", "SSLv3"}, false);
    private static SslRMIClientSocketFactory clientSocketFactory = new MyClientSocketFactory();

    public static SslRMIServerSocketFactory getServerSockFactory() {
        return serverSocketFactory;
    }

    public static SslRMIClientSocketFactory getClientSockFactory() {
        return clientSocketFactory;
    }

    static {
        log.log(Level.FINE, "Enabeld SSL Server protocols: " + serverSocketFactory.getEnabledProtocols());
        log.log(Level.FINE, "Enabeld SSL ciphers: " + serverSocketFactory.getEnabledCipherSuites());
    }

    //---------------------------------    
    /*
    private final static RMIServerSocketFactory serverSocketFactory =
    (null == RMISocketFactory.getSocketFactory())
    ? RMISocketFactory.getDefaultSocketFactory()
    : RMISocketFactory.getSocketFactory();
    private final static RMIClientSocketFactory clientSocketFactory = new LittleRMISocketFactory();
     * 
     */
    public LittleRemoteObject() throws RemoteException {
        super(registryPort, clientSocketFactory, serverSocketFactory);
    }
}
