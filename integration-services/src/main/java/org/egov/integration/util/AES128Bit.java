package org.egov.integration.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AES128Bit {
	//static private final String ENCODING = "UTF-8";
    static private final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    //static private final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    //static private final String TRANSFORMATION = "AES/CBC/NoPadding";
    //static private final String TRANSFORMATION = "AES/CBC/PKCS5NOPADDING";
	//static private final String TRANSFORMATION = "AES/CTR/NoPadding";
    static private final String AES = "AES";

    public static String doEncryptedAES(String inputString, String key) {
        String encrypted = "error_encrypted";
        byte[] encryptedbyte = null;
        byte[] keyByte = null;
        Cipher cp;
        SecretKeySpec sks = null;
        IvParameterSpec ips = null;
        try {
        	//encryptedbyte = inputString.getBytes(ENCODING);
        	encryptedbyte = inputString.getBytes(StandardCharsets.UTF_16LE);
            keyByte = getKeyBytes(key);
        } catch (NullPointerException | DigestException e) {
            e.printStackTrace();
            return encrypted;
        }

        sks = new SecretKeySpec(keyByte, AES);
        ips = new IvParameterSpec(keyByte);

        try {
            cp = Cipher.getInstance(TRANSFORMATION);
            cp.init(Cipher.ENCRYPT_MODE, sks, ips);
            encryptedbyte = cp.doFinal(encryptedbyte);
            encrypted = Base64.encodeBase64String(encryptedbyte);
            return encrypted;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return encrypted;
        }
    }

    public static String doDecryptedAES(String encrypted, String key) {
        String decrypted = "error_decrypted";
        byte[] encryptedByte;
        byte[] keyByte;
        try {
        	//encryptedByte = Base64.decodeBase64(encrypted.getBytes("UTF8"));
        	encryptedByte = Base64.decodeBase64(encrypted.getBytes(StandardCharsets.UTF_16LE));
            keyByte = getKeyBytes(key);
        } catch (NullPointerException | DigestException e) {
            System.out.println(e.getMessage());
            return decrypted;
        }
        SecretKeySpec sks = new SecretKeySpec(keyByte, AES);
        IvParameterSpec ips = new IvParameterSpec(keyByte);
        try {
            Cipher cp = Cipher.getInstance(TRANSFORMATION);
            cp.init(Cipher.DECRYPT_MODE, sks, ips);
            encryptedByte = cp.doFinal(encryptedByte);
            //decrypted = new String(encryptedByte, ENCODING);
            decrypted = new String(encryptedByte, StandardCharsets.UTF_16LE);
            return decrypted;
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            System.out.println(e.getMessage());
            return decrypted;
        }
    }
    
    private static byte[] getKeyBytes(String key) throws DigestException {
        /*byte[] keyBytes = new byte[16];
        try {
            byte[] parameterKeyBytes = key.getBytes(ENCODING);
            System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.length, keyBytes.length));
        } catch (UnsupportedEncodingException e) {
            System.out.println("[Error][AES][getKeyBytes][0]: " + e.getMessage());
        }
        return keyBytes;*/
    	
    	PWDeriveBytes secretKey = new PWDeriveBytes(key, key.getBytes(StandardCharsets.US_ASCII));    	
    	return secretKey.getBytes(16);
    }
}
