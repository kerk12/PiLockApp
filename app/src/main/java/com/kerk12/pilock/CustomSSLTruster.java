package com.kerk12.pilock;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by kgiannakis on 3/4/2017.
 */

public class CustomSSLTruster {

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean DoesCertFileExist(){
        File cert = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "pilock.crt");
        return cert.exists();
    }

    /**
     * Used to read the certificate from a file. It looks for the pilock.crt file, and returns an InputStream that can be processed.
     * @return InputStream of the certificate file.
     * @throws FileNotFoundException If pilock.crt wasn't found.
     */
    private static FileInputStream ReadCert() throws FileNotFoundException {
        FileInputStream fis = null;
        //Read from internal storage. WARNING: Needs the appropriate permission.
        File cert = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "pilock.crt");

        fis = new FileInputStream(cert);
        return fis;
    }

    /**
     * Static method used to mark a supplied certificate as trusted.
     * Taken from @see <a href="https://developer.android.com/training/articles/security-ssl.html#UnknownCa">https://developer.android.com/training/articles/security-ssl.html#UnknownCa</a>
     * @return A new SSLContext, used in connections.
     * @throws IOException If an error occurs while reading the file.
     * @throws GeneralSecurityException If a security related error occurs.
     */
    public static SSLContext TrustCertificate() throws IOException, GeneralSecurityException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = null;

        FileInputStream certFis = null;
        certFis = ReadCert();
        if (certFis == null){
            throw new FileNotFoundException();
        }
        caInput = new BufferedInputStream(certFis);

        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        return context;

    }
}
