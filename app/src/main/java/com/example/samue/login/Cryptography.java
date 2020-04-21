package com.example.samue.login;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ALEX for P2PSharing.2.1
 *
 *
 */
public class Cryptography {
    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_RSA_SIGN = "SHA256withRSA";
    private static final String ALGORITHM_AES = "AES";
    static final int SIZE_ENCRYPT = 1024;
    static final int SIZE_CIFER = 256;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKeySpec secretKey;


    public Cryptography(){
        this.privateKey = null;
        this.publicKey = null;
        this.secretKey = null;
    }

    public java.security.PrivateKey getPrivateKey() {
        return privateKey;
    }

    public java.security.PublicKey getPublicKey() {
        return publicKey;
    }
    public SecretKeySpec getSecretKey() { return secretKey; }

    public void setPublicKey(java.security.PublicKey key) {
        this.publicKey = key;
    }

    public void setSecretKey(SecretKeySpec key) { this.secretKey = key; }

    public String getPrivateKeyString(){
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(this.privateKey.getEncoded());
        return bytesToString(pkcs8EncodedKeySpec.getEncoded());
    }
    public String getPublicKeyString(){
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(this.publicKey.getEncoded());
        return bytesToString(x509EncodedKeySpec.getEncoded());
    }
    public String getSecretKeyString(){
        return bytesToString(secretKey.getEncoded());
    }

    public void setPrivateKeyString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeySpecException {
        byte[] encodedPrivateKey = stringToBytes(key);

        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privKey = keyFactory.generatePrivate(privateKeySpec);
        this.privateKey = privKey;
    }
    public void setPublicKeyString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException{
        byte[] encodedPublicKey = stringToBytes(key);

        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey pubKey = keyFactory.generatePublic(publicKeySpec);
        this.publicKey = pubKey;
    }
    public void setSecretKeyString(String key)   {
        byte[] encodedSecretKey = stringToBytes(key);
        SecretKeySpec secretKey = new SecretKeySpec(encodedSecretKey, ALGORITHM_AES);
        this.secretKey =secretKey;
    }

    public void genKeyPair () throws NoSuchAlgorithmException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        kpg.initialize(SIZE_ENCRYPT);
        KeyPair kp = kpg.genKeyPair();
        this.privateKey = kp.getPrivate();
        this.publicKey = kp.getPublic();

    }
    public void generateKey () throws NoSuchAlgorithmException {

        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGen.init(SIZE_CIFER);
        SecretKey secretKey = keyGen.generateKey();
        byte[] bytesSecretKey = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytesSecretKey, ALGORITHM_AES);
        this.secretKey = secretKeySpec;
    }

    public String cipherRSA(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] encodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        cipher.init(Cipher.ENCRYPT_MODE,this.publicKey);
        encodedBytes = cipher.doFinal(text.getBytes());
        //encodedBytes es un dato binario, no podemos pasarlo a string directamente, lo codificamos
        result = bytesToString(encodedBytes);
        return result;
    }
    public String cipherRSA(byte[] text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] encodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        cipher.init(Cipher.ENCRYPT_MODE,this.publicKey);
        encodedBytes = cipher.doFinal(text);
        //encodedBytes es un dato binario, no podemos pasarlo a string directamente, lo codificamos
        //result sin llamar a la funcion bytes to string
        // result = new BigInteger(encodedBytes).toString(36);
        result = bytesToString(encodedBytes);
        return result;
    }
    public String decipherRSAToString(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] decodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        cipher.init(Cipher.DECRYPT_MODE,this.privateKey);
        decodedBytes = cipher.doFinal(stringToBytes(text));
        result = new String(decodedBytes);
        return result;
    }
    public byte[] decipherRSA(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] result;
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        cipher.init(Cipher.DECRYPT_MODE,this.privateKey);
        result = cipher.doFinal(stringToBytes(text));

        return result;
    }
    public String signRSA(String text) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] message = text.getBytes();
        Signature s = Signature.getInstance(ALGORITHM_RSA_SIGN);
        s.initSign(this.privateKey);
        s.update(message);
        byte[] signature = s.sign();
        String result = bytesToString(signature);
        return result;
    }
    public boolean verifyRSA(String sign, String text) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] message = text.getBytes();
        byte[] signature = stringToBytes(sign);
        boolean valid = false;
        Signature s = Signature.getInstance(ALGORITHM_RSA_SIGN);
        s.initVerify(this.publicKey);
        s.update(message);
        valid = s.verify(signature);
        return valid;
    }
    public String cipherSimetric(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] encodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(Cipher.ENCRYPT_MODE,this.secretKey);
        encodedBytes = cipher.doFinal(text.getBytes());
        result = bytesToString(encodedBytes);
        return result;
    }
    public String cipherSimetric(byte[] text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] encodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(Cipher.ENCRYPT_MODE,this.secretKey);
        encodedBytes = cipher.doFinal(text);
        result = bytesToString(encodedBytes);
        return result;
    }
    public String decipherSimetricToString(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] decodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(Cipher.DECRYPT_MODE,this.secretKey);
        decodedBytes = cipher.doFinal(stringToBytes(text));
        result = new String(decodedBytes);
        return result;
    }
    public byte[] decipherSimetric(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        byte[] result;
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(Cipher.DECRYPT_MODE,this.secretKey);
        result = cipher.doFinal(stringToBytes(text));

        return result;
    }




    //funcion que sustituye a estas codificaciones, y se reutiliza:
    // (cipher) -> result = new BigInteger(encodedBytes).toString(36);
    //result = new String(Base64.encode(encodedBytes, Base64.DEFAULT));
    public static String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }
    //funcion que sustituye a las siguientes codificaciones, para tener las funciones y usar en mas puntos:
    //(decipher) -> decodedBytes = cipher.doFinal(new BigInteger(text,36).toByteArray());
    //decodedBytes = cipher.doFinal(Base64.decode(text, Base64.DEFAULT));
    public static byte[] stringToBytes(String s) {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }

    //funciones para usar las APIs KEY de PUBNUB
    public String pubnub(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] decodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(stringToBytes(Utils.PUBNUB), ALGORITHM_AES));
        decodedBytes = cipher.doFinal(stringToBytes(text));
        result = new String(decodedBytes);
        return result;
    }



}
