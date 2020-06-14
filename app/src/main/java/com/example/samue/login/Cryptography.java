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
    private static final int SIZE_ENCRYPT = 2048;
    private static final int SIZE_CIFER = 256;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKeySpec secretKey;


    public Cryptography(){
        this.privateKey = null;
        this.publicKey = null;
        this.secretKey = null;
    }

    public String getPublicKeyString(){
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(this.publicKey.getEncoded());
        return bytesToString(x509EncodedKeySpec.getEncoded());
    }
    public String getSecretKeyString(){
        return bytesToString(secretKey.getEncoded());
    }

    public void setPublicKeyString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException{
        byte[] encodedPublicKey = stringToBytes(key);

        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);
    }
    public void setSecretKeyString(String key)   {
        byte[] encodedSecretKey = stringToBytes(key);
        this.secretKey = new SecretKeySpec(encodedSecretKey, ALGORITHM_AES);
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
        SecretKey secKey = keyGen.generateKey();
        byte[] bytesSecretKey = secKey.getEncoded();
        this.secretKey = new SecretKeySpec(bytesSecretKey, ALGORITHM_AES);
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
    public String decipherRSAToString(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String result;
        byte[] decodedBytes;
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
        cipher.init(Cipher.DECRYPT_MODE,this.privateKey);
        decodedBytes = cipher.doFinal(stringToBytes(text));
        result = new String(decodedBytes);
        return result;
    }
    public String signRSA(String text) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] message = text.getBytes();
        Signature s = Signature.getInstance(ALGORITHM_RSA_SIGN);
        s.initSign(this.privateKey);
        s.update(message);
        byte[] signature = s.sign();
        return bytesToString(signature);
    }
    public boolean verifyRSA(String sign, String text) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] message = text.getBytes();
        byte[] signature = stringToBytes(sign);
        boolean valid;
        Signature s = Signature.getInstance(ALGORITHM_RSA_SIGN);
        s.initVerify(this.publicKey);
        s.update(message);
        valid = s.verify(signature);
        return valid;
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
    private static byte[] stringToBytes(String s) {
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
