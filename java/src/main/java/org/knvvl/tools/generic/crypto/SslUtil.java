package org.knvvl.tools.generic.crypto;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SslUtil.class);

    private static final TrustManager[] TRUST_ALL_TRUSTMANAGERs = new TrustManager[]{new TrustAll()};

    private SslUtil()
    {
    }

    /**
     * Create a SSLContext that trusts all certificates
     *
     * @return The SSLContext that can be used to trust everything
     */
    public static SSLContext getTrustAllContext()
    {
        try
        {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, TRUST_ALL_TRUSTMANAGERs, new SecureRandom());
            return sslContext;
        }
        catch (KeyManagementException | NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("Failed to setup SSL context without warnings.", e);
        }
    }

    private static class TrustAll extends X509ExtendedTrustManager
    {
        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {
            printCertificateInfo(chain);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {
            printCertificateInfo(chain);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException
        {
            printCertificateInfo(chain);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException
        {
            printCertificateInfo(chain);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException
        {
            printCertificateInfo(chain);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException
        {
            printCertificateInfo(chain);
        }

        private void printCertificateInfo(X509Certificate[] chain)
        {
            for (X509Certificate cert : chain)
            {
                LOGGER.warn("==== Ignoring Certificate Warnings for ====");
                LOGGER.warn("  Type: {}", cert.getType());
                LOGGER.warn("  Issuer: {}", cert.getIssuerX500Principal());
                LOGGER.warn("  Subject: {}", cert.getSubjectX500Principal());
                LOGGER.warn("  Expires: {}", cert.getNotAfter());
                LOGGER.warn("===========================================");
            }
        }
    }
}
