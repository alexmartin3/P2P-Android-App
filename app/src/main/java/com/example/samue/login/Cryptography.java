package com.example.samue.login;

import org.spongycastle.jcajce.provider.asymmetric.RSA;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by ALEX for P2PSharing.2.1
 *
 *
 */
public class Cryptography {
    static final String ALGORITHM = "RSA";
    static final int SIZE_ALGORITHM = 1024;
    private PrivateKey privateKey;
    private PublicKey publicKey;


    public Cryptography() throws NoSuchAlgorithmException {
        genKeyPair(SIZE_ALGORITHM);
    }

    public java.security.PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(java.security.PrivateKey key) {
        privateKey = key;
    }

    public java.security.PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(java.security.PublicKey key) {
        publicKey = key;
    }
    private void genKeyPair (int size) throws NoSuchAlgorithmException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(size);
        KeyPair kp = kpg.genKeyPair();
        this.privateKey = kp.getPrivate();
        this.publicKey = kp.getPublic();

    }
    public String cipher (String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] encodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE,this.publicKey);
        encodedBytes = cipher.doFinal(text.getBytes());
        //encodedBytes es un dato binario, no podemos pasarlo a string directamente, lo codificamos
        result = new BigInteger(encodedBytes).toString(36);
        return result;
    }
    public String decipher (String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] decodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE,this.privateKey);
        //operacion contraria, pasamos el string a dato binario
        decodedBytes = cipher.doFinal(new BigInteger(text,36).toByteArray());
        result = new String(decodedBytes);
        return result;
    }



}
