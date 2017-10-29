package com.kerk12.pilock;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;


public class KeystoreHelper {

    public static final String ALIAS = "PiLock";
    private KeyStore keyStore;
    private Context context;


    /**
     * Main constructor method.
     * @param c The context of the app.
     */
    public KeystoreHelper(Context c) {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            context = c;
            CreateKeys();
        } catch (Exception e) {
        }


    }

    /**
     * Creates the keypairs using the provided alias.
     */
    private void CreateKeys() {
        try {
            if (!keyStore.containsAlias(ALIAS)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(ALIAS)
                        .setSubject(new X500Principal("CN=" + ALIAS))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                KeyPairGenerator gen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                gen.initialize(spec);
                gen.generateKeyPair();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Encrypt a string using the Keystore.
     * @param string The string to be encrypted.
     * @return A base64 representation of the encrypted bytes.
     */
    public String Encrypt(String string){
        try {
            // Get the private key entry.
            KeyStore.PrivateKeyEntry prkeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);
            // Get the public key.
            RSAPublicKey pubkey = (RSAPublicKey) prkeyEntry.getCertificate().getPublicKey();

            // Create a new RSA cipher and initialize it with the public key.
            Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            input.init(Cipher.ENCRYPT_MODE, pubkey);

            // Feed the strings to be encrypted into a CipherOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, input);
            cipherOutputStream.write(string.getBytes("UTF-8"));
            cipherOutputStream.close();

            // Get the encrypted bytes
            byte [] vals = outputStream.toByteArray();

            // Return a Base64 Representation of the bytes.
            return Base64.encodeToString(vals, Base64.DEFAULT);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt a base64 encoded, encrypted string using the Keystore.
     * @param cipherText The B64 encoded ciphertext.
     * @return The decrypted plaintext.
     */
    public String Decrypt(String cipherText){
        try {
            // Get the Private key entry.
            KeyStore.PrivateKeyEntry prkeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);

            // Create a new RSA Decryption cipher and initialize it with the private key.
            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            output.init(Cipher.DECRYPT_MODE, prkeyEntry.getPrivateKey());

            // Feed the text into a CipherInputStream and get the decrypted bytes. Store them in an ArrayList.
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            // Create a new byte array containing all the bytes from the arraylist.
            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            // Encode the bytes to UTF-8.
            String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            return finalText;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}