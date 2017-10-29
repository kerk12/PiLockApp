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

/**
 * Created by kerk12 on 10/29/17.
 */

public class KeystoreHelper {

    public static final String ALIAS = "PiLock";
    private KeyStore keyStore;
    private Context context;


    public KeystoreHelper(Context c) {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            context = c;
            CreateKeys();
        } catch (Exception e) {
        }


    }

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

    public String Encrypt(String string){
        try {
            KeyStore.PrivateKeyEntry prkeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);
            RSAPublicKey pubkey = (RSAPublicKey) prkeyEntry.getCertificate().getPublicKey();

            Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            input.init(Cipher.ENCRYPT_MODE, pubkey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, input);
            cipherOutputStream.write(string.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte [] vals = outputStream.toByteArray();

            return Base64.encodeToString(vals, Base64.DEFAULT);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String Decrypt(String cipherText){
        try {
            KeyStore.PrivateKeyEntry prkeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);


            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            output.init(Cipher.DECRYPT_MODE, prkeyEntry.getPrivateKey());

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            return finalText;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}