package org.egov.integration.util;

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
    static private final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    static private final String AES = "AES";

    public static String doEncryptedAES(String inputString, String key) {
        String encrypted = "error_encrypted";
        byte[] encryptedbyte = null;
        Cipher cp;
        SecretKeySpec sks = null;
        IvParameterSpec ips = null;
        PasswordDeriveBytes secretKey;
        try {
        	encryptedbyte = inputString.getBytes(StandardCharsets.UTF_16LE);
            secretKey = getKeyBytes(key);            
        } catch (NullPointerException | DigestException e) {
            e.printStackTrace();
            return encrypted;
        } 

        try {
			sks = new SecretKeySpec(secretKey.GetBytes(16), AES);
			ips = new IvParameterSpec(secretKey.GetBytes(16));
		} catch (DigestException e1) {
			e1.printStackTrace();
		}

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
        PasswordDeriveBytes secretKey;
        try {
        	encryptedByte = Base64.decodeBase64(encrypted.getBytes(StandardCharsets.UTF_16LE));
        	secretKey = getKeyBytes(key);
        } catch (NullPointerException | DigestException e) {
            return decrypted;
        }
        SecretKeySpec sks = null;
        IvParameterSpec ips = null;
		try {
			sks = new SecretKeySpec(secretKey.GetBytes(16), AES);
			ips = new IvParameterSpec(secretKey.GetBytes(16));
		} catch (DigestException e1) {
			e1.printStackTrace();
		}
        
        try {
            Cipher cp = Cipher.getInstance(TRANSFORMATION);
            cp.init(Cipher.DECRYPT_MODE, sks, ips);
            encryptedByte = cp.doFinal(encryptedByte);
            decrypted = new String(encryptedByte, StandardCharsets.UTF_16LE);
            return decrypted;
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            System.out.println(e.getMessage());
            return decrypted;
        }
    }
    
    private static PasswordDeriveBytes getKeyBytes(String key) throws DigestException {    	
    	PasswordDeriveBytes secretKey = new PasswordDeriveBytes(key, String.valueOf(key.length()).getBytes(StandardCharsets.US_ASCII));
    	return secretKey;
    }
    
    public static void main(String[] args) {
    	String secretKey = "Adfhj#$@56677745";
		
		String encryptedEmpCode = doEncryptedAES("1975010001Z", secretKey);
		System.out.println("encryptedEmpCode :: " + encryptedEmpCode);
		String encryptedMonth = doEncryptedAES("03", secretKey);
		System.out.println("encryptedMonth :: " + encryptedMonth);
		String encryptedYear = doEncryptedAES("2018", secretKey);
		System.out.println("encryptedYear :: " + encryptedYear);
		
		System.out.println("decryptedEmpCode : " + doDecryptedAES(encryptedEmpCode, secretKey));
		System.out.println("decryptedMonth : " + doDecryptedAES(encryptedMonth, secretKey));
		System.out.println("decryptedYear : " + doDecryptedAES(encryptedYear, secretKey));
	}
}
